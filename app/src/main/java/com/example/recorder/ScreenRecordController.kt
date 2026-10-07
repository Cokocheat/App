package com.example.recorder

import android.content.Context
import android.content.Intent
import com.example.model.RecordingConfig
import com.example.model.RecordingState
import com.example.model.RecordStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object ScreenRecordController {

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    // Temporary storage for MediaProjection tokens from Activity
    var resultCode: Int = 0
    var resultData: Intent? = null

    // Callbacks to service or listeners
    var onStopRequested: (() -> Unit)? = null
    var onPauseResumeRequested: (() -> Unit)? = null

    fun updateStatus(status: RecordStatus) {
        _state.value = _state.value.copy(status = status, errorMessage = null)
    }

    fun updateTicker(seconds: Long, bytes: Long) {
        _state.value = _state.value.copy(
            durationSeconds = seconds,
            estimatedBytes = bytes
        )
    }

    fun updateCountdown(remaining: Int) {
        _state.value = _state.value.copy(
            status = if (remaining > 0) RecordStatus.COUNTDOWN else _state.value.status,
            countdownRemaining = remaining
        )
    }

    fun setRecordingStarted(file: File, config: RecordingConfig) {
        _state.value = _state.value.copy(
            status = RecordStatus.RECORDING,
            durationSeconds = 0L,
            estimatedBytes = 0L,
            countdownRemaining = 0,
            currentOutputFile = file,
            currentConfig = config,
            errorMessage = null
        )
    }

    fun setRecordingStopped(lastSavedFile: File? = null) {
        _state.value = _state.value.copy(
            status = RecordStatus.IDLE,
            durationSeconds = 0L,
            estimatedBytes = 0L,
            countdownRemaining = 0,
            currentOutputFile = lastSavedFile,
            errorMessage = null
        )
        resultData = null
        resultCode = 0
    }

    fun setError(message: String) {
        _state.value = _state.value.copy(
            status = RecordStatus.IDLE,
            countdownRemaining = 0,
            errorMessage = message
        )
        resultData = null
        resultCode = 0
    }

    fun requestStop(context: Context) {
        val intent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun requestPauseResume(context: Context) {
        val intent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.ACTION_PAUSE_RESUME
        }
        context.startService(intent)
    }
}
