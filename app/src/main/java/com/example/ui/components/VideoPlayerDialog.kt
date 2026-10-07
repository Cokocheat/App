package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RecordedVideo
import com.example.ui.theme.*
import java.io.File

@Composable
fun VideoPlayerDialog(
    video: RecordedVideo,
    onDismiss: () -> Unit
) {
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("video_player_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = StudioSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${video.resolution} • ${video.fps} FPS • ${video.formattedDuration}",
                            fontSize = 12.sp,
                            color = UltraCyan
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_player")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Video Player View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mc = MediaController(ctx)
                                mc.setAnchorView(this)
                                setMediaController(mc)
                                setVideoURI(Uri.fromFile(File(video.filePath)))
                                setOnPreparedListener { mp ->
                                    mediaPlayerRef = mp
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        runCatching {
                                            val params = mp.playbackParams
                                            params.speed = playbackSpeed
                                            mp.playbackParams = params
                                        }
                                    }
                                    mp.start()
                                }
                            }
                        },
                        update = { videoView ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                mediaPlayerRef?.let { mp ->
                                    runCatching {
                                        val params = mp.playbackParams
                                        params.speed = playbackSpeed
                                        mp.playbackParams = params
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slow-Motion / Speed control bar (Essential for reviewing 120 FPS footage!)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SlowMotionVideo,
                            contentDescription = "Slow Motion",
                            tint = UltraCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Analisis Kecepatan (Uji Kehalusan 120 FPS):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val speeds = listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f)
                        speeds.forEach { sp ->
                            val isSelected = playbackSpeed == sp
                            FilterChip(
                                selected = isSelected,
                                onClick = { playbackSpeed = sp },
                                label = {
                                    Text(
                                        text = "${sp}x ${if (sp < 1.0f) "Slow-Mo" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UltraCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = StudioSurface,
                                    labelColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
