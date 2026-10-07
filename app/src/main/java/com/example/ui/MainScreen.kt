package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AudioSourceOption
import com.example.model.RecordStatus
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val recordState by viewModel.recordState.collectAsStateWithLifecycle()
    val config by viewModel.configFlow.collectAsStateWithLifecycle()
    val deviceSpecs by viewModel.deviceSpecs.collectAsStateWithLifecycle()
    val videos by viewModel.videosFlow.collectAsStateWithLifecycle()
    val selectedVideo by viewModel.selectedVideoForPlayback.collectAsStateWithLifecycle()
    val isFloatingWidgetVisible by viewModel.floatingWidgetVisible.collectAsStateWithLifecycle()

    // Activity Result Launcher for MediaProjection (Screen Recording Intent)
    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startServiceRecording(result.resultCode, result.data!!)
        } else {
            viewModel.cancelCountdown()
        }
    }

    // Runtime Permission Launcher for RECORD_AUDIO and POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        mediaProjectionManager?.let { mgr ->
            projectionLauncher.launch(mgr.createScreenCaptureIntent())
        }
    }

    val launchRecordFlow: () -> Unit = {
        val permissions = mutableListOf<String>()
        if (config.audioSource != AudioSourceOption.MUTE) {
            permissions.add(android.Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        } else {
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            mediaProjectionManager?.let { mgr ->
                projectionLauncher.launch(mgr.createScreenCaptureIntent())
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_screen_scaffold"),
        containerColor = StudioBackground,
        topBar = {
            TopBar(
                deviceSpecs = deviceSpecs,
                isRecording = recordState.isActive
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.RECORDER,
                    onClick = { viewModel.setTab(ScreenTab.RECORDER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "Perekam",
                            tint = if (currentTab == ScreenTab.RECORDER) RecCrimson else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Studio",
                            fontWeight = if (currentTab == ScreenTab.RECORDER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RecCrimson,
                        selectedTextColor = RecCrimson,
                        indicatorColor = StudioSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_recorder")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.GALLERY,
                    onClick = { viewModel.setTab(ScreenTab.GALLERY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Galeri",
                            tint = if (currentTab == ScreenTab.GALLERY) UltraCyan else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Galeri (${videos.size})",
                            fontWeight = if (currentTab == ScreenTab.GALLERY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = UltraCyan,
                        selectedTextColor = UltraCyan,
                        indicatorColor = StudioSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_gallery")
                )

                NavigationBarItem(
                    selected = currentTab == ScreenTab.SETTINGS,
                    onClick = { viewModel.setTab(ScreenTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = if (currentTab == ScreenTab.SETTINGS) TechViolet else TextSecondary
                        )
                    },
                    label = {
                        Text(
                            text = "Pengaturan",
                            fontWeight = if (currentTab == ScreenTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TechViolet,
                        selectedTextColor = TechViolet,
                        indicatorColor = StudioSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_animation"
            ) { tab ->
                when (tab) {
                    ScreenTab.RECORDER -> {
                        StudioScreen(
                            recordingState = recordState,
                            config = config,
                            deviceSpecs = deviceSpecs,
                            onRecordButtonClick = {
                                viewModel.onRecordButtonClicked(launchRecordFlow)
                            },
                            onPauseResumeClick = {
                                viewModel.togglePauseResume()
                            },
                            onConfigUpdate = { viewModel.updateConfig(it) },
                            onApplyPreset = { viewModel.applyPreset(it) },
                            onToggleFloatingWidget = { viewModel.toggleFloatingWidget() },
                            isFloatingWidgetVisible = isFloatingWidgetVisible
                        )
                    }

                    ScreenTab.GALLERY -> {
                        GalleryScreen(
                            videos = videos,
                            onPlayVideo = { viewModel.selectVideoForPlayback(it) },
                            onDeleteVideo = { viewModel.deleteVideo(it) },
                            onRenameVideo = { video, name -> viewModel.renameVideo(video, name) },
                            onNavigateToStudio = { viewModel.setTab(ScreenTab.RECORDER) }
                        )
                    }

                    ScreenTab.SETTINGS -> {
                        SettingsScreen(
                            config = config,
                            deviceSpecs = deviceSpecs,
                            onConfigUpdate = { viewModel.updateConfig(it) }
                        )
                    }
                }
            }

            // Draggable Floating Controls Simulator (if enabled)
            if (isFloatingWidgetVisible) {
                FloatingControlWidget(
                    recordingState = recordState,
                    onToggleRecord = {
                        viewModel.onRecordButtonClicked(launchRecordFlow)
                    },
                    onPauseResume = {
                        viewModel.togglePauseResume()
                    },
                    onClose = {
                        viewModel.toggleFloatingWidget()
                    }
                )
            }

            // Countdown Overlay
            if (recordState.status == RecordStatus.COUNTDOWN && recordState.countdownRemaining > 0) {
                CountdownOverlay(
                    secondsRemaining = recordState.countdownRemaining,
                    onCancel = { viewModel.cancelCountdown() }
                )
            }

            // Video Player Dialog (with Slow-Mo controls)
            selectedVideo?.let { video ->
                VideoPlayerDialog(
                    video = video,
                    onDismiss = { viewModel.selectVideoForPlayback(null) }
                )
            }
        }
    }
}
