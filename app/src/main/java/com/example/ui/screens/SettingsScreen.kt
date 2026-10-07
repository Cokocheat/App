package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.DeviceSpecs
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    config: RecordingConfig,
    deviceSpecs: DeviceSpecs,
    onConfigUpdate: (RecordingConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 120 FPS Highlight Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C1929),
            border = BorderStroke(1.5.dp, UltraCyan.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(UltraCyan)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "120 FPS SUPPORT",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = "Ultra Smooth Engine",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = UltraCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "RecStudio mendukung perekaman hingga 120 frame per detik (FPS) untuk kelancaran gameplay tingkat e-sports. Disarankan menggunakan bitrate minimal 24 Mbps untuk ketajaman visual terbaik.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // ================= SECTION 1: VIDEO SETTINGS =================
        SettingsSectionHeader(
            title = "Pengaturan Video",
            icon = Icons.Default.Videocam,
            iconColor = UltraCyan
        )

        // Frame Rate (FPS)
        SettingsCard {
            Text(
                text = "Frame Rate (Kecepatan Bingkai)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Pilih 120 FPS untuk kehalusan maksimal pada game kompetitif",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FrameRateOption.values().forEach { fpsOpt ->
                    val isSelected = config.frameRate == fpsOpt
                    Surface(
                        onClick = { onConfigUpdate(config.copy(frameRate = fpsOpt)) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) (if (fpsOpt.isUltra) UltraCyan else RecCrimson) else StudioCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fps_option_${fpsOpt.fps}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = fpsOpt.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected && fpsOpt.isUltra) UltraCyan else TextPrimary
                                    )
                                    if (fpsOpt.isUltra) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) UltraCyan else Color(0xFF003840))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ULTRA",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) Color.Black else UltraCyan
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = fpsOpt.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onConfigUpdate(config.copy(frameRate = fpsOpt)) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (fpsOpt.isUltra) UltraCyan else RecCrimson
                                )
                            )
                        }
                    }
                }
            }
        }

        // Resolusi Video
        SettingsCard {
            Text(
                text = "Resolusi Video",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                VideoResolution.values().forEach { resOpt ->
                    val isSelected = config.resolution == resOpt
                    Surface(
                        onClick = { onConfigUpdate(config.copy(resolution = resOpt)) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) UltraCyan else StudioCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = resOpt.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { onConfigUpdate(config.copy(resolution = resOpt)) },
                                colors = RadioButtonDefaults.colors(selectedColor = UltraCyan)
                            )
                        }
                    }
                }
            }
        }

        // Bitrate Video
        SettingsCard {
            Text(
                text = "Bitrate Video (Kejernihan)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Bitrate tinggi mencegah blur dan artefak piksel saat gerakan cepat",
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BitrateOption.values().forEach { bitOpt ->
                    val isSelected = config.bitrate == bitOpt
                    Surface(
                        onClick = { onConfigUpdate(config.copy(bitrate = bitOpt)) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) UltraCyan else StudioCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bitOpt.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TextPrimary
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { onConfigUpdate(config.copy(bitrate = bitOpt)) },
                                colors = RadioButtonDefaults.colors(selectedColor = UltraCyan)
                            )
                        }
                    }
                }
            }
        }

        // Video Codec & Orientasi
        SettingsCard {
            Text(
                text = "Format Enkoder Video (Codec)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VideoCodec.values().forEach { codec ->
                    val isSelected = config.videoCodec == codec
                    Surface(
                        onClick = { onConfigUpdate(config.copy(videoCodec = codec)) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) UltraCyan else StudioCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = codec.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) UltraCyan else TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = codec.description,
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ================= SECTION 2: AUDIO SETTINGS =================
        SettingsSectionHeader(
            title = "Pengaturan Audio",
            icon = Icons.Default.Mic,
            iconColor = TechGreen
        )

        SettingsCard {
            Text(
                text = "Sumber Audio",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AudioSourceOption.values().forEach { audioOpt ->
                    val isSelected = config.audioSource == audioOpt
                    Surface(
                        onClick = { onConfigUpdate(config.copy(audioSource = audioOpt)) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) StudioSurfaceVariant else StudioSurface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) TechGreen else StudioCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = audioOpt.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = audioOpt.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onConfigUpdate(config.copy(audioSource = audioOpt)) },
                                colors = RadioButtonDefaults.colors(selectedColor = TechGreen)
                            )
                        }
                    }
                }
            }
        }

        // ================= SECTION 3: CONTROLS & SMART FEATURES =================
        SettingsSectionHeader(
            title = "Kontrol & Fitur Pintar",
            icon = Icons.Default.TouchApp,
            iconColor = TechAmber
        )

        SettingsCard {
            // Countdown Picker
            Text(
                text = "Hitung Mundur Sebelum Merekam",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val countdowns = listOf(0, 3, 5, 10)
                countdowns.forEach { cd ->
                    val isSelected = config.countdownSeconds == cd
                    FilterChip(
                        selected = isSelected,
                        onClick = { onConfigUpdate(config.copy(countdownSeconds = cd)) },
                        label = {
                            Text(
                                text = if (cd == 0) "Mati" else "${cd} Detik",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TechAmber,
                            selectedLabelColor = Color.Black,
                            containerColor = StudioSurface,
                            labelColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = StudioCardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Shake to Stop Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Goyang HP untuk Stop",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Guncangkan perangkat untuk menghentikan rekaman tanpa membuka aplikasi",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
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

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = StudioCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Stop on Screen Off
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hentikan saat Layar Mati",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Menekan tombol power akan otomatis menyimpan video",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = config.stopOnScreenOff,
                    onCheckedChange = { onConfigUpdate(config.copy(stopOnScreenOff = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = UltraCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = StudioCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Haptic Feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Getaran Responsif (Haptic)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Getar saat hitung mundur dan saat rekaman dimulai/berhenti",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = config.hapticFeedback,
                    onCheckedChange = { onConfigUpdate(config.copy(hapticFeedback = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = UltraCyan
                    )
                )
            }
        }

        // ================= SECTION 4: HARDWARE & SYSTEM INFO =================
        SettingsSectionHeader(
            title = "Hardware & Informasi",
            icon = Icons.Default.Info,
            iconColor = TextMuted
        )

        SettingsCard {
            InfoRow("Refresh Rate Layar", "${deviceSpecs.refreshRateHz} Hz")
            HorizontalDivider(color = StudioCardBorder, modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Dukungan 120 FPS", if (deviceSpecs.is120HzSupported) "Hardware 120Hz Aktif" else "120 FPS Encoder Aktif")
            HorizontalDivider(color = StudioCardBorder, modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Resolusi Layar Fisik", deviceSpecs.screenResolution)
            HorizontalDivider(color = StudioCardBorder, modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Sisa Ruang Penyimpanan", String.format("%.2f GB Bebas", deviceSpecs.freeStorageGb))
            HorizontalDivider(color = StudioCardBorder, modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Folder Rekaman", "Movies/RecStudio")
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioSurfaceVariant,
        border = BorderStroke(1.dp, StudioCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
