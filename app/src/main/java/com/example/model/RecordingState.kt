package com.example.model

import java.io.File

enum class RecordStatus {
    IDLE,
    COUNTDOWN,
    RECORDING,
    PAUSED,
    SAVING
}

data class RecordingState(
    val status: RecordStatus = RecordStatus.IDLE,
    val durationSeconds: Long = 0L,
    val estimatedBytes: Long = 0L,
    val countdownRemaining: Int = 0,
    val currentOutputFile: File? = null,
    val currentConfig: RecordingConfig = RecordingConfig(),
    val errorMessage: String? = null
) {
    val isRecording: Boolean
        get() = status == RecordStatus.RECORDING

    val isPaused: Boolean
        get() = status == RecordStatus.PAUSED

    val isActive: Boolean
        get() = status == RecordStatus.RECORDING || status == RecordStatus.PAUSED

    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val secs = durationSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format("%02d:%02d", minutes, secs)
            }
        }

    val formattedSize: String
        get() {
            val mb = estimatedBytes / (1024.0 * 1024.0)
            return if (mb >= 1000.0) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
