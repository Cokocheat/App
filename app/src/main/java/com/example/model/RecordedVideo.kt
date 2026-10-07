package com.example.model

import android.net.Uri

data class RecordedVideo(
    val id: String,
    val title: String,
    val filePath: String,
    val uri: Uri,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String,
    val fps: Int,
    val timestamp: Long
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024.0) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
