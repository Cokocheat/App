package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("rec_studio_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<RecordingConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): RecordingConfig {
        val resName = prefs.getString("resolution", VideoResolution.FHD_1080P.name) ?: VideoResolution.FHD_1080P.name
        val fpsName = prefs.getString("frame_rate", FrameRateOption.FPS_120.name) ?: FrameRateOption.FPS_120.name
        val bitrateName = prefs.getString("bitrate", BitrateOption.B_24_MBPS.name) ?: BitrateOption.B_24_MBPS.name
        val codecName = prefs.getString("codec", VideoCodec.H264.name) ?: VideoCodec.H264.name
        val orientationName = prefs.getString("orientation", RecordOrientation.AUTO.name) ?: RecordOrientation.AUTO.name
        val audioSourceName = prefs.getString("audio_source", AudioSourceOption.MIC.name) ?: AudioSourceOption.MIC.name
        val audioBitrateName = prefs.getString("audio_bitrate", AudioBitrateOption.KBPS_256.name) ?: AudioBitrateOption.KBPS_256.name
        val audioSampleRateName = prefs.getString("audio_sample_rate", AudioSampleRateOption.HZ_48000.name) ?: AudioSampleRateOption.HZ_48000.name
        val countdown = prefs.getInt("countdown", 3)
        val shakeToStop = prefs.getBoolean("shake_to_stop", true)
        val shakeSensitivity = prefs.getFloat("shake_sensitivity", 14.5f)
        val showFloating = prefs.getBoolean("show_floating", true)
        val stopOnScreenOff = prefs.getBoolean("stop_screen_off", true)
        val haptic = prefs.getBoolean("haptic", true)

        val resolution = runCatching { VideoResolution.valueOf(resName) }.getOrDefault(VideoResolution.FHD_1080P)
        val fps = runCatching { FrameRateOption.valueOf(fpsName) }.getOrDefault(FrameRateOption.FPS_120)
        val bitrate = runCatching { BitrateOption.valueOf(bitrateName) }.getOrDefault(BitrateOption.B_24_MBPS)
        val codec = runCatching { VideoCodec.valueOf(codecName) }.getOrDefault(VideoCodec.H264)
        val orientation = runCatching { RecordOrientation.valueOf(orientationName) }.getOrDefault(RecordOrientation.AUTO)
        val audioSource = runCatching { AudioSourceOption.valueOf(audioSourceName) }.getOrDefault(AudioSourceOption.MIC)
        val audioBitrate = runCatching { AudioBitrateOption.valueOf(audioBitrateName) }.getOrDefault(AudioBitrateOption.KBPS_256)
        val audioSampleRate = runCatching { AudioSampleRateOption.valueOf(audioSampleRateName) }.getOrDefault(AudioSampleRateOption.HZ_48000)

        return RecordingConfig(
            resolution = resolution,
            frameRate = fps,
            bitrate = bitrate,
            videoCodec = codec,
            orientation = orientation,
            audioSource = audioSource,
            audioBitrate = audioBitrate,
            audioSampleRate = audioSampleRate,
            countdownSeconds = countdown,
            shakeToStop = shakeToStop,
            shakeSensitivity = shakeSensitivity,
            showFloatingControls = showFloating,
            stopOnScreenOff = stopOnScreenOff,
            hapticFeedback = haptic
        )
    }

    fun updateConfig(newConfig: RecordingConfig) {
        prefs.edit()
            .putString("resolution", newConfig.resolution.name)
            .putString("frame_rate", newConfig.frameRate.name)
            .putString("bitrate", newConfig.bitrate.name)
            .putString("codec", newConfig.videoCodec.name)
            .putString("orientation", newConfig.orientation.name)
            .putString("audio_source", newConfig.audioSource.name)
            .putString("audio_bitrate", newConfig.audioBitrate.name)
            .putString("audio_sample_rate", newConfig.audioSampleRate.name)
            .putInt("countdown", newConfig.countdownSeconds)
            .putBoolean("shake_to_stop", newConfig.shakeToStop)
            .putFloat("shake_sensitivity", newConfig.shakeSensitivity)
            .putBoolean("show_floating", newConfig.showFloatingControls)
            .putBoolean("stop_screen_off", newConfig.stopOnScreenOff)
            .putBoolean("haptic", newConfig.hapticFeedback)
            .apply()

        _configFlow.value = newConfig
    }
}
