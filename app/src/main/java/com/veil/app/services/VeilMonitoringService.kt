package com.veil.app.services

import android.app.Activity
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.veil.app.core.TemporalDetectionEvaluator
import java.util.concurrent.atomic.AtomicBoolean
import java.nio.ByteBuffer

/** One explicit Android consent result creates one non-restarting capture session. */
class VeilMonitoringService : Service() {
    private lateinit var workerThread: HandlerThread
    private lateinit var worker: Handler
    private val main = Handler(android.os.Looper.getMainLooper())
    private val stopping = AtomicBoolean(false)
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private var classifier: ScreenClassifier? = null
    private var framePixels: ByteBuffer? = null
    private val evaluator = TemporalDetectionEvaluator()
    private var lastFrameAt = 0L
    private var lastPackage: String? = null
    private var visible = true
    private var captureAspect = 0f
    private val healthCheck = object : Runnable {
        override fun run() {
            if (stopping.get()) return
            if (!CommitmentStore.get(this@VeilMonitoringService).accessibilityConnected.value) {
                finishSession("App intervention access was revoked. Monitoring stopped.")
                return
            }
            if (lastFrameAt > 0L && SystemClock.elapsedRealtime() - lastFrameAt > MAX_FRAME_AGE_MS) {
                evaluator.reset()
                MonitoringRuntime.updateStatus("Screen sharing active · no recent frames available for analysis")
            }
            worker.postDelayed(this, 1_000L)
        }
    }

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() = finishSession("Screen sharing stopped. Start again to grant new consent.")
        override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (!isVisible) evaluator.reset()
        }
        override fun onCapturedContentResize(width: Int, height: Int) {
            if (height > 0 && kotlin.math.abs(width.toFloat() / height - captureAspect) > 0.05f) {
                finishSession("Screen size changed. Restart monitoring with fresh consent.")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        workerThread = HandlerThread("VeilLocalInference").also { it.start() }
        worker = Handler(workerThread.looper)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Screen analysis", NotificationManager.IMPORTANCE_LOW),
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            finishSession("Monitoring stopped by you.")
            return START_NOT_STICKY
        }
        val resultCode = intent?.getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val resultData = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<Intent>(RESULT_DATA)
        }
        if (intent?.action != ACTION_START || resultCode != Activity.RESULT_OK || resultData == null) {
            finishSession("Screen sharing consent is required.")
            return START_NOT_STICKY
        }
        if (stopping.get()) return START_NOT_STICKY
        MonitoringRuntime.updateRunning(true)
        try {
            startForeground(NOTIFICATION_ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } catch (_: Exception) {
            finishSession("Android could not start screen analysis. Grant consent again from Veil.")
            return START_NOT_STICKY
        }
        worker.post { startCapture(resultCode, resultData) }
        return START_NOT_STICKY
    }

    private fun startCapture(resultCode: Int, resultData: Intent) {
        if (stopping.get() || projection != null) return
        if (!CommitmentStore.get(this).accessibilityConnected.value) {
            finishSession("Enable disclosed app intervention access before screen analysis.")
            return
        }
        MonitoringRuntime.updateStatus("Checking local classifier…")
        when (val initialized = TensorFlowLiteInitializer().initialize(this)) {
            is ClassifierInitialization.Ready -> classifier = initialized.classifier
            is ClassifierInitialization.MissingModel -> {
                finishSession("AI unavailable: no reviewed local classifier is bundled.")
                return
            }
            is ClassifierInitialization.Failed -> {
                finishSession("AI unavailable: local classifier validation failed.")
                return
            }
        }
        if (stopping.get()) return // A stop while native initialization ran wins over starting capture.
        try {
            val bounds = getSystemService(WindowManager::class.java).maximumWindowMetrics.bounds
            captureAspect = bounds.width().toFloat() / bounds.height()
            val scale = minOf(1f, 720f / maxOf(bounds.width(), bounds.height()))
            val width = (bounds.width() * scale).toInt().coerceAtLeast(1)
            val height = (bounds.height() * scale).toInt().coerceAtLeast(1)
            reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2).also {
                it.setOnImageAvailableListener({ source -> consumeLatestFrame(source) }, worker)
            }
            val session = getSystemService(MediaProjectionManager::class.java)
                .getMediaProjection(resultCode, resultData)
            projection = session // Retain before later setup can fail so cleanup can always stop it.
            session.registerCallback(projectionCallback, worker)
            display = session.createVirtualDisplay(
                "Veil local analysis", width, height, resources.displayMetrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader!!.surface, null, worker,
            )
            MonitoringRuntime.updateStatus("Screen sharing active · waiting for an eligible app · protected screens unavailable")
            worker.postDelayed(healthCheck, 1_000L)
        } catch (_: Exception) {
            finishSession("Screen analysis could not start. Return to Veil and grant new consent.")
        }
    }

    private fun consumeLatestFrame(source: ImageReader) {
        val image = try { source.acquireLatestImage() } catch (_: IllegalStateException) { null } ?: return
        try {
            if (stopping.get()) return
            val now = SystemClock.elapsedRealtime()
            if (now - lastFrameAt < FRAME_INTERVAL_MS) return
            if (now - lastFrameAt > MAX_FRAME_AGE_MS) evaluator.reset()
            lastFrameAt = now
            val observation = MonitoringRuntime.foregroundObservation()
            val packageName = observation.packageName
            val store = CommitmentStore.get(this)
            if (!visible || !store.accessibilityConnected.value ||
                !getSystemService(PowerManager::class.java).isInteractive ||
                getSystemService(KeyguardManager::class.java).isKeyguardLocked ||
                packageName == null || isEssentialPackage(this, packageName) ||
                store.state.value.cooldownRemaining(now, store.bootCount()) > 0
            ) {
                evaluator.reset()
                lastPackage = null
                MonitoringRuntime.updateStatus("Screen sharing active · analysis paused on this screen")
                return
            }
            if (packageName != lastPackage) {
                evaluator.reset()
                lastPackage = packageName
                return // Discard a transition frame before attributing subsequent scores.
            }
            val bitmap = copyFrame(image)
            try {
                val score = classifier?.classify(bitmap)?.explicitConfidence ?: return
                if (stopping.get() || SystemClock.elapsedRealtime() - now > MAX_FRAME_AGE_MS) {
                    evaluator.reset()
                    return
                }
                if (!score.isFinite() || score !in 0f..1f) {
                    finishSession("AI analysis stopped: invalid classifier output.")
                    return
                }
                if (MonitoringRuntime.foregroundObservation() != observation ||
                    !store.accessibilityConnected.value ||
                    !getSystemService(PowerManager::class.java).isInteractive ||
                    getSystemService(KeyguardManager::class.java).isKeyguardLocked
                ) {
                    evaluator.reset()
                    return
                }
                MonitoringRuntime.updateStatus("Local analysis active · protected screens may be unavailable")
                if (evaluator.observe(score)) {
                    evaluator.reset()
                    store.intervene(score, packageName)
                }
            } finally {
                bitmap.recycle()
            }
        } catch (_: Exception) {
            finishSession("Local analysis failed. Monitoring stopped; focus sessions are unchanged.")
        } finally {
            image.close()
        }
    }

    private fun copyFrame(image: Image): Bitmap {
        val plane = image.planes[0]
        check(plane.pixelStride == 4) { "Unsupported frame pixel layout" }
        val rowBytes = image.width * 4
        val size = rowBytes * image.height
        val pixels = framePixels?.takeIf { it.capacity() == size }
            ?: ByteBuffer.allocateDirect(size).also { framePixels = it }
        pixels.clear()
        val source = plane.buffer.duplicate()
        val base = source.position()
        // The last image row need not include trailing padding. Copy only actual pixels per row.
        for (row in 0 until image.height) {
            val start = base + row * plane.rowStride
            source.limit(plane.buffer.limit())
            source.position(start)
            source.limit(start + rowBytes)
            pixels.put(source)
        }
        pixels.flip()
        val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        try {
            bitmap.copyPixelsFromBuffer(pixels)
            return bitmap
        } catch (error: Exception) {
            bitmap.recycle()
            throw error
        }
    }

    private fun finishSession(reason: String) {
        if (!stopping.compareAndSet(false, true)) return
        MonitoringRuntime.updateStatus(reason)
        worker.post {
            releaseCapture()
            main.post {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun releaseCapture() {
        worker.removeCallbacks(healthCheck)
        runCatching { reader?.setOnImageAvailableListener(null, null) }
        runCatching { display?.release() }
        display = null
        runCatching { reader?.close() }
        reader = null
        runCatching { projection?.unregisterCallback(projectionCallback) }
        runCatching { projection?.stop() }
        projection = null
        runCatching { classifier?.close() }
        classifier = null
        framePixels?.let { pixels ->
            pixels.clear()
            while (pixels.hasRemaining()) pixels.put(0.toByte())
        }
        framePixels = null
        evaluator.reset()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finishSession("Screen configuration changed. Restart monitoring with fresh consent.")
    }

    override fun onDestroy() {
        MonitoringRuntime.updateRunning(false)
        if (stopping.compareAndSet(false, true)) MonitoringRuntime.updateStatus("Monitoring stopped. New consent is required.")
        worker.post {
            releaseCapture()
            workerThread.quitSafely()
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun notification(): Notification {
        val stop = PendingIntent.getService(
            this, 0, Intent(this, VeilMonitoringService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Veil screen sharing")
            .setContentText("Local screen analysis requested. Tap Stop to end sharing.")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stop)
            .build()
    }

    companion object {
        const val RESULT_CODE = "projection_result_code"
        const val RESULT_DATA = "projection_result_data"
        const val ACTION_START = "com.veil.app.START_MONITORING"
        const val ACTION_STOP = "com.veil.app.STOP_MONITORING"
        const val CHANNEL_ID = "veil_protection"
        const val NOTIFICATION_ID = 1001
        private const val FRAME_INTERVAL_MS = 500L
        private const val MAX_FRAME_AGE_MS = 2_000L
    }
}
