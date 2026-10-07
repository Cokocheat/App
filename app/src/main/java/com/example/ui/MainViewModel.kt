package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SettingsRepository
import com.example.data.VideoRepository
import com.example.model.*
import com.example.recorder.FloatingOverlayService
import com.example.recorder.ScreenRecordController
import com.example.recorder.ScreenRecordService
import android.provider.Settings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ScreenTab {
    RECORDER,
    GALLERY,
    SETTINGS
}

data class DeviceSpecs(
    val refreshRateHz: Int = 60,
    val screenResolution: String = "1080x2400",
    val freeStorageGb: Double = 0.0,
    val is120HzSupported: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    val videoRepo = VideoRepository(application)

    val configFlow: StateFlow<RecordingConfig> = settingsRepo.configFlow
    val recordState: StateFlow<RecordingState> = ScreenRecordController.state
    val videosFlow: StateFlow<List<RecordedVideo>> = videoRepo.videos

    private val _currentTab = MutableStateFlow(ScreenTab.RECORDER)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(DeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _selectedVideoForPlayback = MutableStateFlow<RecordedVideo?>(null)
    val selectedVideoForPlayback: StateFlow<RecordedVideo?> = _selectedVideoForPlayback.asStateFlow()

    private val _floatingWidgetVisible = MutableStateFlow(false)
    val floatingWidgetVisible: StateFlow<Boolean> = _floatingWidgetVisible.asStateFlow()

    private val _showOverlayPermissionDialog = MutableStateFlow(false)
    val showOverlayPermissionDialog: StateFlow<Boolean> = _showOverlayPermissionDialog.asStateFlow()

    fun promptOverlayPermission() {
        _showOverlayPermissionDialog.value = true
    }

    fun dismissOverlayPermissionDialog() {
        _showOverlayPermissionDialog.value = false
    }

    fun onOverlayPermissionGranted(context: Context) {
        _showOverlayPermissionDialog.value = false
        _floatingWidgetVisible.value = true
        updateConfig(configFlow.value.copy(showFloatingControls = true))
        FloatingOverlayService.start(context)
    }

    fun checkAndSyncFloatingOverlay(context: Context) {
        val hasOverlay = Settings.canDrawOverlays(context)
        val shouldShow = hasOverlay && configFlow.value.showFloatingControls
        _floatingWidgetVisible.value = shouldShow
        if (shouldShow) {
            FloatingOverlayService.start(context)
        }
    }

    fun toggleFloatingWidget(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            _showOverlayPermissionDialog.value = true
            return
        }
        val newState = !_floatingWidgetVisible.value
        _floatingWidgetVisible.value = newState
        updateConfig(configFlow.value.copy(showFloatingControls = newState))
        if (newState) {
            FloatingOverlayService.start(context)
        } else {
            FloatingOverlayService.stop(context)
        }
    }

    private var countdownJob: Job? = null

    init {
        detectDeviceSpecs()
        viewModelScope.launch {
            videoRepo.refreshVideos()
        }
        viewModelScope.launch {
            // refresh videos whenever recording finishes
            recordState.collect { state ->
                if (state.status == RecordStatus.IDLE && state.currentOutputFile != null) {
                    videoRepo.refreshVideos()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun detectDeviceSpecs() {
        try {
            val context = getApplication<Application>()
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val refreshRate = display.refreshRate.toInt()

            val metrics = android.util.DisplayMetrics()
            display.getRealMetrics(metrics)
            val resStr = "${metrics.widthPixels} x ${metrics.heightPixels}"

            val stat = StatFs(Environment.getDataDirectory().path)
            val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
            val freeGb = freeBytes / (1024.0 * 1024.0 * 1024.0)

            _deviceSpecs.value = DeviceSpecs(
                refreshRateHz = refreshRate,
                screenResolution = resStr,
                freeStorageGb = freeGb,
                is120HzSupported = refreshRate >= 115
            )
        } catch (e: Exception) {
            _deviceSpecs.value = DeviceSpecs(
                refreshRateHz = 60,
                screenResolution = "1080 x 1920",
                freeStorageGb = 16.0,
                is120HzSupported = false
            )
        }
    }

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun updateConfig(config: RecordingConfig) {
        settingsRepo.updateConfig(config)
    }

    fun applyPreset(presetName: String) {
        val current = configFlow.value
        val newConfig = when (presetName) {
            "120FPS_GAMING" -> current.copy(
                resolution = VideoResolution.FHD_1080P,
                frameRate = FrameRateOption.FPS_120,
                bitrate = BitrateOption.B_24_MBPS,
                videoCodec = VideoCodec.H264,
                audioSource = AudioSourceOption.MIC
            )
            "4K_MASTER" -> current.copy(
                resolution = VideoResolution.UHD_4K,
                frameRate = FrameRateOption.FPS_60,
                bitrate = BitrateOption.B_50_MBPS,
                videoCodec = VideoCodec.HEVC
            )
            "YOUTUBE_60" -> current.copy(
                resolution = VideoResolution.FHD_1080P,
                frameRate = FrameRateOption.FPS_60,
                bitrate = BitrateOption.B_16_MBPS
            )
            "BALANCED" -> current.copy(
                resolution = VideoResolution.HD_720P,
                frameRate = FrameRateOption.FPS_30,
                bitrate = BitrateOption.B_8_MBPS
            )
            else -> current
        }
        updateConfig(newConfig)
    }

    fun onRecordButtonClicked(
        onLaunchProjectionPermission: () -> Unit
    ) {
        val currentState = recordState.value
        if (currentState.isActive) {
            // Stop recording
            stopRecording()
        } else if (currentState.status == RecordStatus.COUNTDOWN) {
            // Cancel countdown
            cancelCountdown()
        } else {
            // Start recording flow
            val cfg = configFlow.value
            if (cfg.countdownSeconds > 0) {
                startCountdown(cfg.countdownSeconds, onLaunchProjectionPermission)
            } else {
                onLaunchProjectionPermission()
            }
        }
    }

    private fun startCountdown(seconds: Int, onComplete: () -> Unit) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                ScreenRecordController.updateCountdown(i)
                vibrateCountdown()
                delay(1000L)
            }
            ScreenRecordController.updateCountdown(0)
            onComplete()
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        ScreenRecordController.setRecordingStopped()
    }

    fun startServiceRecording(resultCode: Int, resultData: Intent) {
        ScreenRecordController.resultCode = resultCode
        ScreenRecordController.resultData = resultData

        val context = getApplication<Application>()
        val cfg = configFlow.value

        val intent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_START
            putExtra(ScreenRecordService.EXTRA_CONFIG_RES, cfg.resolution.name)
            putExtra(ScreenRecordService.EXTRA_CONFIG_FPS, cfg.frameRate.name)
            putExtra(ScreenRecordService.EXTRA_CONFIG_BITRATE, cfg.bitrate.name)
            putExtra(ScreenRecordService.EXTRA_CONFIG_CODEC, cfg.videoCodec.name)
            putExtra(ScreenRecordService.EXTRA_CONFIG_AUDIO, cfg.audioSource.name)
            putExtra(ScreenRecordService.EXTRA_CONFIG_SHAKE, cfg.shakeToStop)
            putExtra(ScreenRecordService.EXTRA_CONFIG_SCREEN_OFF, cfg.stopOnScreenOff)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopRecording() {
        cancelCountdown()
        val context = getApplication<Application>()
        ScreenRecordController.requestStop(context)
    }

    fun togglePauseResume() {
        val context = getApplication<Application>()
        ScreenRecordController.requestPauseResume(context)
    }

    fun selectVideoForPlayback(video: RecordedVideo?) {
        _selectedVideoForPlayback.value = video
    }

    fun deleteVideo(video: RecordedVideo) {
        viewModelScope.launch {
            videoRepo.deleteVideo(video)
        }
    }

    fun renameVideo(video: RecordedVideo, newName: String) {
        viewModelScope.launch {
            videoRepo.renameVideo(video, newName)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateCountdown() {
        val context = getApplication<Application>()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (configFlow.value.hapticFeedback && vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(40)
            }
        }
    }
}
