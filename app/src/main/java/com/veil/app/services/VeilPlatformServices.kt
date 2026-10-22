package com.veil.app.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import com.veil.app.MainActivity

/**
 * Receives only the window lifecycle events needed by the future protection adapter.
 * Raw events and text are intentionally ignored and never logged.
 */
class VeilAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: View? = null
    private var foreground: String? = null
    private val store by lazy { CommitmentStore.get(this) }
    private val tick = object : Runnable {
        override fun run() { store.tick(); render(); handler.postDelayed(this, 1000) }
    }
    override fun onServiceConnected() { store.connection(true); handler.removeCallbacks(tick); handler.post(tick) }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        store.connection(true)
        if (pkg == packageName && overlay != null && event.className?.toString() != MainActivity::class.java.name) return
        foreground = pkg
        val essential = isEssentialPackage(this, pkg)
        val reason = when {
            pkg == packageName -> MonitoringRuntime.ForegroundReason.SELF
            pkg == "com.android.systemui" -> MonitoringRuntime.ForegroundReason.SYSTEM_UI
            essential -> MonitoringRuntime.ForegroundReason.ESSENTIAL
            else -> MonitoringRuntime.ForegroundReason.APP
        }
        MonitoringRuntime.updateForegroundPackage(pkg.takeUnless { essential }, reason)
        render()
    }
    private fun render() {
        val pkg = foreground
        val power = getSystemService(android.os.PowerManager::class.java)
        val locked = getSystemService(android.app.KeyguardManager::class.java).isKeyguardLocked
        val reason = if (store.accessibilityConnected.value && pkg != null && power.isInteractive && !locked) store.blockingReason(pkg) else null
        if (reason == null) { removeOverlay(); return }
        if (overlay != null) return
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40); setBackgroundColor(Color.rgb(23, 24, 28))
        }
        panel.addView(Button(this).apply {
            text = "Emergency exit — end restrictions"
            setOnClickListener { MonitoringRuntime.stop(this@VeilAccessibilityService); store.endFocus(); store.dismissCooldown(); removeOverlay() }
        }, LinearLayout.LayoutParams(-1, -2))
        panel.addView(TextView(this).apply {
            text = "VEIL\n\n$reason\n\nLeave this app, open recovery, or exit protection at any time."
            setTextColor(Color.WHITE); textSize = 22f; gravity = Gravity.CENTER
        })
        fun button(label: String, action: () -> Unit) {
            panel.addView(Button(this).apply { text = label; setOnClickListener { action() } },
                LinearLayout.LayoutParams(-1, -2).apply { topMargin = 16 })
        }
        button("Leave this app") {
            leaveCurrentSurface()
            runCatching { startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
        button("Open recovery") {
            leaveCurrentSurface()
            runCatching { startActivity(Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_RECOVERY, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)) }
        }
        if (store.state.value.cooldownPackage == pkg) {
            button("Was this intervention correct?") {
                leaveCurrentSurface()
                runCatching { startActivity(Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_RECOVERY, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)) }
            }
            button("Incorrect detection — stop guardian") {
                com.veil.app.services.FeedbackStore.get(this).report(com.veil.app.core.Correction.NOT_EXPLICIT)
                MonitoringRuntime.stop(this); store.dismissCooldown(true); removeOverlay()
            }
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(panel) }
        val params = WindowManager.LayoutParams(-1, -1, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        try { getSystemService(WindowManager::class.java).addView(scroll, params); overlay = scroll }
        catch (_: RuntimeException) { foreground = null; MonitoringRuntime.updateForegroundPackage(null); store.connection(false); MonitoringRuntime.stop(this) }
    }
    private fun leaveCurrentSurface() {
        // Window removal/Home launch can coalesce accessibility events. Do not let the
        // timer reattach the previous app's overlay while navigation is in flight.
        foreground = null
        MonitoringRuntime.updateForegroundPackage(null, MonitoringRuntime.ForegroundReason.LEAVING)
        removeOverlay()
    }
    private fun removeOverlay() {
        overlay?.let { view -> runCatching { getSystemService(WindowManager::class.java).removeView(view) } }; overlay = null
    }
    override fun onInterrupt() {
        // Android interrupts accessibility feedback (for example speech) on focus changes.
        // This is not an unbind or permission revocation. Veil emits no speech feedback.
        // Keep foreground/session state; actual unbind/destroy below owns disconnection.
    }
    override fun onUnbind(intent: Intent?): Boolean { cleanup(); return super.onUnbind(intent) }
    override fun onDestroy() { cleanup(); super.onDestroy() }
    private fun cleanup() {
        handler.removeCallbacksAndMessages(null); foreground = null; MonitoringRuntime.updateForegroundPackage(null)
        removeOverlay(); store.connection(false); MonitoringRuntime.stop(this)
    }
}
