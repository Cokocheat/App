package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.DeviceSpecs
import com.example.ui.theme.*

@Composable
fun StudioScreen(
    recordingState: RecordingState,
    config: RecordingConfig,
    deviceSpecs: DeviceSpecs,
    onRecordButtonClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onConfigUpdate: (RecordingConfig) -> Unit,
    onApplyPreset: (String) -> Unit,
    onToggleFloatingWidget: () -> Unit,
    isFloatingWidgetVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Pulsing animation for record button and active recording ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_recorder")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (recordingState.isRecording) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (recordingState.isRecording) 600 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error Message Banner (if any)
        recordingState.errorMessage?.let { error ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Active Recording Status Card (when recording or paused)
        AnimatedVisibility(visible = recordingState.isActive) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = StudioSurfaceVariant,
                border = BorderStroke(1.dp, if (recordingState.isRecording) RecCrimson else TechAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_recording_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (recordingState.isRecording) RecCrimson else TechAmber)
                            )
                            Text(
                                text = if (recordingState.isRecording) "SEDANG MEREKAM" else "DIJEDA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (recordingState.isRecording) RecCrimson else TechAmber
                            )
                        }

                        Text(
                            text = "${config.resolution.badge} • ${config.frameRate.fps} FPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UltraCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Digital Timer
                    Text(
                        text = recordingState.formattedDuration,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Estimasi Ukuran: ${recordingState.formattedSize}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons in active card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPauseResumeClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_card_pause_resume"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (recordingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = UltraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (recordingState.isPaused) "Lanjutkan" else "Jeda",
                                color = UltraCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onRecordButtonClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_card_stop"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RecCrimson
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Hentikan",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hentikan",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Giant Studio Record Button
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer Glowing Ripple Ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (recordingState.isActive) RecCrimson.copy(alpha = 0.18f)
                        else UltraCyan.copy(alpha = 0.08f)
                    )
                    .border(
                        1.5.dp,
                        if (recordingState.isActive) RecCrimson.copy(alpha = 0.4f)
                        else UltraCyan.copy(alpha = 0.3f),
                        CircleShape
                    )
            )

            // Middle Ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        if (recordingState.isActive) RecCrimson.copy(alpha = 0.28f)
                        else StudioSurfaceVariant
                    )
                    .border(
                        2.dp,
                        if (recordingState.isActive) RecCrimson else UltraCyan.copy(alpha = 0.6f),
                        CircleShape
                    )
            )

            // Center Primary Touch Button
            Surface(
                onClick = onRecordButtonClick,
                shape = CircleShape,
                color = if (recordingState.isActive) RecCrimson else Color(0xFFD6183C),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(120.dp)
                    .testTag("main_record_button")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (recordingState.isActive) Icons.Default.Stop else Icons.Default.Videocam,
                        contentDescription = "Tombol Rekam Layar",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (recordingState.isActive) "STOP" else "REKAM",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    if (!recordingState.isActive) {
                        Text(
                            text = "${config.frameRate.fps} FPS",
                            color = UltraCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Profile Summary Dock
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = StudioSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Profil Rekaman Aktif",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${config.resolution.badge} • ${config.frameRate.title} • ${config.bitrate.label.split(" ")[0]}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // 120 FPS High Framerate Toggle Chip
                val is120 = config.frameRate == FrameRateOption.FPS_120
                Surface(
                    onClick = {
                        val nextFps = if (is120) FrameRateOption.FPS_60 else FrameRateOption.FPS_120
                        onConfigUpdate(config.copy(frameRate = nextFps))
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = if (is120) UltraCyan else StudioSurface,
                    border = BorderStroke(1.dp, UltraCyan),
                    modifier = Modifier.testTag("toggle_120fps_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "120 FPS",
                            tint = if (is120) Color.Black else UltraCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (is120) "120 FPS AKTIF" else "AKTIFKAN 120 FPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (is120) Color.Black else UltraCyan
                        )
                    }
                }
            }
        }

        // Preset Profiles Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Preset Cepat",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetCard(
                    title = "Gaming Pro",
                    fps = "120 FPS",
                    res = "1080p",
                    badgeColor = UltraCyan,
                    isSelected = config.frameRate == FrameRateOption.FPS_120 && config.resolution == VideoResolution.FHD_1080P,
                    onClick = { onApplyPreset("120FPS_GAMING") },
                    modifier = Modifier.weight(1f)
                )

                PresetCard(
                    title = "Master UHD",
                    fps = "60 FPS",
                    res = "4K 2160p",
                    badgeColor = TechViolet,
                    isSelected = config.resolution == VideoResolution.UHD_4K,
                    onClick = { onApplyPreset("4K_MASTER") },
                    modifier = Modifier.weight(1f)
                )

                PresetCard(
                    title = "YouTube",
                    fps = "60 FPS",
                    res = "1080p FHD",
                    badgeColor = RecCrimson,
                    isSelected = config.frameRate == FrameRateOption.FPS_60 && config.resolution == VideoResolution.FHD_1080P,
                    onClick = { onApplyPreset("YOUTUBE_60") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Controls Toggles
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = StudioSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Alat Cepat & Kontrol",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Goyang",
                            tint = UltraCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Goyang HP untuk Stop",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hentikan rekaman seketika saat diguncang",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = config.shakeToStop,
                        onCheckedChange = { onConfigUpdate(config.copy(shakeToStop = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = UltraCyan
                        )
                    )
                }

                HorizontalDivider(color = StudioCardBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ControlCamera,
                            contentDescription = "Tombol Melayang",
                            tint = TechAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Widget Kontrol Melayang",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tampilkan tombol melayang saat keluar aplikasi",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isFloatingWidgetVisible,
                        onCheckedChange = { onToggleFloatingWidget() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TechAmber
                        )
                    )
                }
            }
        }

        // Hardware Capability Insight Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F1724),
            border = BorderStroke(1.dp, StudioCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Hardware",
                        tint = UltraCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Hardware Screen Capabilities",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UltraCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Refresh Rate Layar Anda: ${deviceSpecs.refreshRateHz} Hz\n" +
                            "• Mode 120 FPS: ${if (deviceSpecs.is120HzSupported) "Hardware 120Hz Terdeteksi (Optimal)" else "Didukung software encoder (Video disimpan dalam 120 FPS murni)"}\n" +
                            "• Resolusi Asli: ${deviceSpecs.screenResolution}\n" +
                            "• Output File: Format MP4 H.264/HEVC kompatibel untuk editing & YouTube",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun PresetCard(
    title: String,
    fps: String,
    res: String,
    badgeColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) badgeColor else StudioCardBorder
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fps,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = badgeColor
            )
            Text(
                text = res,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
