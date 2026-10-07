package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingState
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FloatingControlWidget(
    recordingState: RecordingState,
    onToggleRecord: () -> Unit,
    onPauseResume: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(40f) }
    var offsetY by remember { mutableFloatStateOf(200f) }
    var expanded by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(StudioSurfaceVariant.copy(alpha = 0.95f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("floating_control_widget")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drag handle / Floating status pill
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (recordingState.isRecording) RecCrimson else UltraCyan)
                )
            }

            if (expanded) {
                if (recordingState.isActive) {
                    Text(
                        text = recordingState.formattedDuration,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Pause / Resume
                    IconButton(
                        onClick = onPauseResume,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("floating_btn_pause_resume")
                    ) {
                        Icon(
                            imageVector = if (recordingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Jeda atau Lanjut",
                            tint = UltraCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Stop
                    IconButton(
                        onClick = onToggleRecord,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("floating_btn_stop")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Hentikan",
                            tint = RecCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Text(
                        text = "120 FPS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = UltraCyan
                    )

                    // Start Record
                    IconButton(
                        onClick = onToggleRecord,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("floating_btn_start")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Mulai Rekam",
                            tint = RecCrimson,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Widget",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
