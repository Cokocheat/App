package com.example.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import com.example.model.RecordedVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class VideoRepository(private val context: Context) {

    private val _videos = MutableStateFlow<List<RecordedVideo>>(emptyList())
    val videos: StateFlow<List<RecordedVideo>> = _videos.asStateFlow()

    // Cache to avoid slow repeated MediaMetadataRetriever reads
    private val metadataCache = ConcurrentHashMap<String, Pair<Long, RecordedVideo>>()

    fun getOutputDirectory(): File {
        val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: File(context.filesDir, "movies")
        val recDir = File(moviesDir, "RecStudio")
        if (!recDir.exists()) {
            recDir.mkdirs()
        }
        return recDir
    }

    suspend fun refreshVideos() = withContext(Dispatchers.IO) {
        val dir = getOutputDirectory()
        val files = dir.listFiles { file ->
            file.isFile && (file.extension.equals("mp4", ignoreCase = true) || file.extension.equals("mkv", ignoreCase = true))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        val list = files.mapNotNull { file ->
            val cached = metadataCache[file.absolutePath]
            if (cached != null && cached.first == file.lastModified()) {
                cached.second
            } else {
                val meta = extractVideoMetadata(file)
                if (meta != null) {
                    metadataCache[file.absolutePath] = Pair(file.lastModified(), meta)
                }
                meta
            }
        }
        _videos.value = list
    }

    private fun extractVideoMetadata(file: File): RecordedVideo? {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = widthStr?.toIntOrNull() ?: 1080
            val height = heightStr?.toIntOrNull() ?: 1920

            val frameRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            val fps = frameRateStr?.toFloatOrNull()?.toInt() ?: 120

            retriever.release()

            val resolution = "${width}x${height}"
            RecordedVideo(
                id = file.name,
                title = file.nameWithoutExtension,
                filePath = file.absolutePath,
                uri = Uri.fromFile(file),
                durationMs = durationMs,
                sizeBytes = file.length(),
                resolution = resolution,
                fps = fps,
                timestamp = file.lastModified()
            )
        }.getOrNull()
    }

    suspend fun deleteVideo(video: RecordedVideo): Boolean = withContext(Dispatchers.IO) {
        val file = File(video.filePath)
        metadataCache.remove(file.absolutePath)
        val deleted = if (file.exists()) file.delete() else false
        if (deleted) {
            refreshVideos()
        }
        deleted
    }

    suspend fun renameVideo(video: RecordedVideo, newTitle: String): Boolean = withContext(Dispatchers.IO) {
        val cleanName = newTitle.trim().replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val sourceFile = File(video.filePath)
        val targetFile = File(sourceFile.parentFile, "$cleanName.mp4")
        if (targetFile.exists()) return@withContext false
        metadataCache.remove(sourceFile.absolutePath)
        val renamed = sourceFile.renameTo(targetFile)
        if (renamed) {
            refreshVideos()
        }
        renamed
    }
}
