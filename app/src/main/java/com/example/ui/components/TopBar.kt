package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DeviceSpecs
import com.example.ui.theme.*

@Composable
fun TopBar(
    deviceSpecs: DeviceSpecs,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StudioSurface,
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_top_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and App Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(RecCrimson, Color(0xFF880E4F))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "RecStudio Icon",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "RecStudio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isRecording) "● MEREKAM AKTIF" else "STUDIO SCREEN RECORDER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRecording) RecCrimson else UltraCyan,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // 120 FPS Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF132238))
                        .border(1.dp, UltraCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(UltraCyan)
                        )
                        Text(
                            text = "120 FPS READY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = UltraCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hardware Telemetry Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Refresh Rate Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Refresh Rate",
                            tint = if (deviceSpecs.is120HzSupported) UltraCyan else TechAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Layar Refresh",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${deviceSpecs.refreshRateHz} Hz ${if (deviceSpecs.is120HzSupported) "(Max 120Hz)" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Storage Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdCard,
                            contentDescription = "Penyimpanan",
                            tint = TechGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Penyimpanan",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                            Text(
                                text = String.format("%.1f GB Bebas", deviceSpecs.freeStorageGb),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
