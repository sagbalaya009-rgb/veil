package com.veil.fixture

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager

/** Test-only synthetic geometric image. No people, explicit media, network, or model code. */
class FixtureActivity : Activity() {
    private lateinit var image: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setDecorFitsSystemWindows(false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        image = BitmapFactory.decodeResource(resources, R.raw.synthetic_trigger,
            BitmapFactory.Options().apply { inScaled = false })
        setContentView(object : View(this) {
            private val paint = Paint().apply { isFilterBitmap = false; isAntiAlias = false }
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                canvas.drawBitmap(image, null, Rect(0, 0, width, height), paint)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        image.recycle()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) window.insetsController?.apply {
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsets.Type.systemBars())
        }
    }
}
