package com.example.recorder

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.MainActivity
import com.example.model.RecordStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

class FloatingOverlayService : Service() {

    companion object {
        const val ACTION_SHOW = "com.example.action.SHOW_OVERLAY"
        const val ACTION_HIDE = "com.example.action.HIDE_OVERLAY"

        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, FloatingOverlayService::class.java).apply {
                    action = ACTION_SHOW
                }
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var floatView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var isExpanded = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var timerTextView: TextView? = null
    private var pauseIcon: ImageView? = null
    private var recordIcon: ImageView? = null
    private var expandedContainer: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> removeOverlayAndStop()
            else -> showOverlay()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        if (floatView != null) return // Already showing

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 350
        }

        floatView = createFloatingView()

        floatView?.setOnTouchListener { _, event ->
            val lp = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = lp.x
                    initialY = lp.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = initialX + (event.rawX - initialTouchX).toInt()
                    lp.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatView, lp)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)
                    if (diffX < 15 && diffY < 15) {
                        // Considered a click/tap
                        toggleExpanded()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(floatView, layoutParams)
            observeRecordingState()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun createFloatingView(): View {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))

            // Dark rounded pill background
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#E6121826")) // translucent studio dark
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), Color.parseColor("#4000E5FF")) // subtle cyan border
            }
            background = bg
        }

        // Circular Dot / Bubble Indicator
        val bubbleIcon = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32))
            val dotBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#FF2D55")) // Crimson red
            }
            background = dotBg
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setColorFilter(Color.WHITE)
            setPadding(dp(6), dp(6), dp(6), dp(6))
        }
        rootLayout.addView(bubbleIcon)

        // Expanded Container (Hidden initially until tapped or recording)
        expandedContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.VISIBLE
            setPadding(dp(8), 0, dp(4), 0)

            // Timer TextView
            timerTextView = TextView(this@FloatingOverlayService).apply {
                text = "120 FPS"
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(dp(4), 0, dp(8), 0)
            }
            addView(timerTextView)

            // Pause / Resume Button
            pauseIcon = ImageView(this@FloatingOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                    setMargins(dp(4), 0, dp(4), 0)
                }
                setImageResource(android.R.drawable.ic_media_pause)
                setColorFilter(Color.parseColor("#00E5FF"))
                setOnClickListener {
                    ScreenRecordController.requestPauseResume(this@FloatingOverlayService)
                }
            }
            addView(pauseIcon)

            // Record / Stop Button
            recordIcon = ImageView(this@FloatingOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                    setMargins(dp(4), 0, dp(4), 0)
                }
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(Color.parseColor("#FF3B30"))
                setOnClickListener {
                    val state = ScreenRecordController.state.value
                    if (state.isActive) {
                        ScreenRecordController.requestStop(this@FloatingOverlayService)
                    } else {
                        // Open main activity to start recording
                        val appIntent = Intent(this@FloatingOverlayService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(appIntent)
                    }
                }
            }
            addView(recordIcon)

            // Close Bubble Button
            val closeIcon = ImageView(this@FloatingOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)).apply {
                    setMargins(dp(6), 0, dp(2), 0)
                }
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(Color.parseColor("#94A3B8"))
                setOnClickListener {
                    removeOverlayAndStop()
                }
            }
            addView(closeIcon)
        }

        rootLayout.addView(expandedContainer)
        return rootLayout
    }

    private fun toggleExpanded() {
        isExpanded = !isExpanded
        expandedContainer?.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    private fun observeRecordingState() {
        serviceScope.launch {
            ScreenRecordController.state.collectLatest { state ->
                if (state.isActive) {
                    isExpanded = true
                    expandedContainer?.visibility = View.VISIBLE
                    timerTextView?.text = state.formattedDuration
                    pauseIcon?.visibility = View.VISIBLE
                    pauseIcon?.setImageResource(
                        if (state.isPaused) android.R.drawable.ic_media_play
                        else android.R.drawable.ic_media_pause
                    )
                    recordIcon?.setImageResource(android.R.drawable.ic_delete)
                } else {
                    timerTextView?.text = "120 FPS"
                    pauseIcon?.visibility = View.GONE
                    recordIcon?.setImageResource(android.R.drawable.ic_media_play)
                }
            }
        }
    }

    private fun removeOverlayAndStop() {
        try {
            if (floatView != null) {
                windowManager?.removeView(floatView)
                floatView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density).toInt()
    }

    override fun onDestroy() {
        try {
            if (floatView != null) {
                windowManager?.removeView(floatView)
                floatView = null
            }
        } catch (e: Exception) {
        }
        serviceScope.cancel()
        super.onDestroy()
    }
}
