package com.example.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecordService : Service() {

    companion object {
        const val ACTION_START = "com.example.action.START"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_PAUSE_RESUME = "com.example.action.PAUSE_RESUME"

        const val EXTRA_CONFIG_RES = "extra_config_res"
        const val EXTRA_CONFIG_FPS = "extra_config_fps"
        const val EXTRA_CONFIG_BITRATE = "extra_config_bitrate"
        const val EXTRA_CONFIG_CODEC = "extra_config_codec"
        const val EXTRA_CONFIG_AUDIO = "extra_config_audio"
        const val EXTRA_CONFIG_SHAKE = "extra_config_shake"
        const val EXTRA_CONFIG_SCREEN_OFF = "extra_config_screen_off"

        private const val CHANNEL_ID = "rec_studio_record_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            stopRecordingProcess()
        }
    }

    private var outputFile: File? = null
    private var isRecording = false
    private var isPaused = false
    private var elapsedSeconds = 0L

    private var sensorManager: SensorManager? = null
    private var shakeDetector: ShakeDetector? = null
    private var screenOffReceiver: BroadcastReceiver? = null

    private var currentConfig = RecordingConfig()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_START -> {
                startForegroundWithNotification()
                readConfigFromIntent(intent)
                startRecordingProcess()
            }
            ACTION_STOP -> {
                stopRecordingProcess()
            }
            ACTION_PAUSE_RESUME -> {
                togglePauseResume()
            }
        }

        return START_NOT_STICKY
    }

    private fun readConfigFromIntent(intent: Intent) {
        val resStr = intent.getStringExtra(EXTRA_CONFIG_RES) ?: VideoResolution.FHD_1080P.name
        val fpsStr = intent.getStringExtra(EXTRA_CONFIG_FPS) ?: FrameRateOption.FPS_120.name
        val bitStr = intent.getStringExtra(EXTRA_CONFIG_BITRATE) ?: BitrateOption.B_24_MBPS.name
        val codecStr = intent.getStringExtra(EXTRA_CONFIG_CODEC) ?: VideoCodec.H264.name
        val audioStr = intent.getStringExtra(EXTRA_CONFIG_AUDIO) ?: AudioSourceOption.MIC.name
        val shake = intent.getBooleanExtra(EXTRA_CONFIG_SHAKE, true)
        val screenOff = intent.getBooleanExtra(EXTRA_CONFIG_SCREEN_OFF, true)

        currentConfig = RecordingConfig(
            resolution = runCatching { VideoResolution.valueOf(resStr) }.getOrDefault(VideoResolution.FHD_1080P),
            frameRate = runCatching { FrameRateOption.valueOf(fpsStr) }.getOrDefault(FrameRateOption.FPS_120),
            bitrate = runCatching { BitrateOption.valueOf(bitStr) }.getOrDefault(BitrateOption.B_24_MBPS),
            videoCodec = runCatching { VideoCodec.valueOf(codecStr) }.getOrDefault(VideoCodec.H264),
            audioSource = runCatching { AudioSourceOption.valueOf(audioStr) }.getOrDefault(AudioSourceOption.MIC),
            shakeToStop = shake,
            stopOnScreenOff = screenOff
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Layanan Perekam Layar",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi kontrol perekaman layar aktif"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(timeStr: String, isPausedState: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(this, ScreenRecordService::class.java).apply {
            action = ACTION_PAUSE_RESUME
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 2, pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (isPausedState) "Dijeda ($timeStr)" else "Merekam: $timeStr • ${currentConfig.frameRate.title}"
        val pauseActionTitle = if (isPausedState) "Lanjutkan" else "Jeda"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RecStudio 120 FPS")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setColor(Color.RED)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, pauseActionTitle, pauseResumePendingIntent)
            .addAction(android.R.drawable.ic_delete, "Hentikan", stopPendingIntent)
            .build()
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("00:00", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (currentConfig.audioSource != AudioSourceOption.MUTE) {
                    serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
            }
            startForeground(NOTIFICATION_ID, notification, serviceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startRecordingProcess() {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        val rCode = ScreenRecordController.resultCode
        val rData = ScreenRecordController.resultData

        if (projectionManager == null || rData == null || rCode == 0) {
            ScreenRecordController.setError("Izin perekaman layar tidak valid.")
            stopSelf()
            return
        }

        try {
            val projection = projectionManager.getMediaProjection(rCode, rData)
            if (projection == null) {
                ScreenRecordController.setError("MediaProjection tidak tersedia.")
                stopSelf()
                return
            }

            // CRITICAL (Android 14+): Must register Callback before creating VirtualDisplay
            projection.registerCallback(projectionCallback, Handler(Looper.getMainLooper()))
            mediaProjection = projection

            setupMediaRecorder()
            setupVirtualDisplay()

            mediaRecorder?.start()
            isRecording = true
            isPaused = false
            elapsedSeconds = 0L

            outputFile?.let { file ->
                ScreenRecordController.setRecordingStarted(file, currentConfig)
            }

            startTicker()
            setupShakeDetector()
            setupScreenOffReceiver()
            if (currentConfig.showFloatingControls && android.provider.Settings.canDrawOverlays(this)) {
                FloatingOverlayService.start(this)
            }
            vibrateDevice(50)

        } catch (e: Exception) {
            e.printStackTrace()
            ScreenRecordController.setError("Gagal memulai rekaman: ${e.localizedMessage ?: "Encoder error"}")
            cleanupRecording()
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    private fun setupMediaRecorder() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val (targetWidth, targetHeight) = currentConfig.resolution.getDimensions(metrics.widthPixels, metrics.heightPixels)
        val fps = currentConfig.frameRate.fps
        val bitrate = if (currentConfig.bitrate.bps > 0) currentConfig.bitrate.bps else 24_000_000

        val moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: File(filesDir, "movies")
        val recDir = File(moviesDir, "RecStudio").apply { if (!exists()) mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        outputFile = File(recDir, "REC_${fps}FPS_$timeStamp.mp4")

        try {
            initializeRecorder(targetWidth, targetHeight, fps, bitrate)
        } catch (e: Exception) {
            // Defensive fallback: If 120 FPS or high bitrate is rejected by device hardware encoder, fallback to 60 FPS
            if (fps > 60) {
                try {
                    mediaRecorder?.reset()
                    mediaRecorder?.release()
                } catch (ignored: Exception) {}
                outputFile = File(recDir, "REC_60FPS_$timeStamp.mp4")
                initializeRecorder(targetWidth, targetHeight, 60, minOf(bitrate, 16_000_000))
            } else {
                throw e
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun initializeRecorder(width: Int, height: Int, fps: Int, bitrate: Int) {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            val hasAudio = currentConfig.audioSource != AudioSourceOption.MUTE
            if (hasAudio) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

            setOutputFile(outputFile?.absolutePath)
            setVideoSize(width, height)
            setVideoEncoder(
                if (currentConfig.videoCodec == VideoCodec.HEVC) MediaRecorder.VideoEncoder.HEVC
                else MediaRecorder.VideoEncoder.H264
            )
            setVideoEncodingBitRate(bitrate)
            setVideoFrameRate(fps)

            if (hasAudio) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(currentConfig.audioBitrate.kbps)
                setAudioSamplingRate(currentConfig.audioSampleRate.hz)
            }

            prepare()
        }
    }

    @Suppress("DEPRECATION")
    private fun setupVirtualDisplay() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val (targetWidth, targetHeight) = currentConfig.resolution.getDimensions(metrics.widthPixels, metrics.heightPixels)
        val densityDpi = metrics.densityDpi

        val surface = mediaRecorder?.surface ?: throw IllegalStateException("Surface belum siap")

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "RecStudioVirtualDisplay",
            targetWidth,
            targetHeight,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive && isRecording) {
                delay(1000L)
                if (!isPaused) {
                    elapsedSeconds++
                    val currentSize = outputFile?.length() ?: 0L
                    ScreenRecordController.updateTicker(elapsedSeconds, currentSize)

                    val timeStr = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
                    val manager = getSystemService(NotificationManager::class.java)
                    manager?.notify(NOTIFICATION_ID, buildNotification(timeStr, false))
                }
            }
        }
    }

    private fun togglePauseResume() {
        if (!isRecording || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        try {
            if (isPaused) {
                mediaRecorder?.resume()
                isPaused = false
                ScreenRecordController.updateStatus(RecordStatus.RECORDING)
            } else {
                mediaRecorder?.pause()
                isPaused = true
                ScreenRecordController.updateStatus(RecordStatus.PAUSED)
            }
            val timeStr = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, buildNotification(timeStr, isPaused))
            vibrateDevice(30)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecordingProcess() {
        vibrateDevice(100)
        cleanupRecording()
        ScreenRecordController.setRecordingStopped(outputFile)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cleanupRecording() {
        tickerJob?.cancel()
        tickerJob = null

        try {
            if (isRecording) {
                mediaRecorder?.stop()
            }
        } catch (e: Exception) {
            // Ignore stop errors if recording was too short
        }

        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) {
        }
        mediaRecorder = null

        virtualDisplay?.release()
        virtualDisplay = null

        try {
            mediaProjection?.unregisterCallback(projectionCallback)
        } catch (e: Exception) {
        }

        try {
            mediaProjection?.stop()
        } catch (e: Exception) {
        }
        mediaProjection = null

        isRecording = false
        isPaused = false

        unregisterSensorsAndReceivers()
    }

    private fun setupShakeDetector() {
        if (!currentConfig.shakeToStop) return
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager != null && accelerometer != null) {
            shakeDetector = ShakeDetector {
                stopRecordingProcess()
            }.apply {
                threshold = currentConfig.shakeSensitivity
            }
            sensorManager?.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun setupScreenOffReceiver() {
        if (!currentConfig.stopOnScreenOff) return
        screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    stopRecordingProcess()
                }
            }
        }
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    private fun unregisterSensorsAndReceivers() {
        shakeDetector?.let { detector ->
            sensorManager?.unregisterListener(detector)
        }
        sensorManager = null
        shakeDetector = null

        screenOffReceiver?.let { receiver ->
            runCatching { unregisterReceiver(receiver) }
        }
        screenOffReceiver = null
    }

    @Suppress("DEPRECATION")
    private fun vibrateDevice(ms: Long) {
        if (!currentConfig.hapticFeedback) return
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(ms)
            }
        }
    }

    override fun onDestroy() {
        cleanupRecording()
        serviceScope.cancel()
        super.onDestroy()
    }
}
