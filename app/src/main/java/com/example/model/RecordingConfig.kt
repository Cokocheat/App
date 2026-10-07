package com.example.model

enum class VideoResolution(val label: String, val width: Int, val height: Int, val badge: String) {
    NATIVE("Resolusi Asli Layar", 0, 0, "Native"),
    UHD_4K("4K Ultra HD (2160p)", 3840, 2160, "4K"),
    QHD_2K("2K Quad HD (1440p)", 2560, 1440, "2K"),
    FHD_1080P("Full HD (1080p)", 1920, 1080, "1080p"),
    HD_720P("HD (720p)", 1280, 720, "720p"),
    SD_480P("SD (480p)", 854, 480, "480p");

    fun getDimensions(displayWidth: Int, displayHeight: Int): Pair<Int, Int> {
        if (this == NATIVE || width == 0 || height == 0) {
            return Pair(displayWidth, displayHeight)
        }
        val isPortrait = displayHeight >= displayWidth
        return if (isPortrait) {
            val targetW = minOf(width, height)
            val targetH = maxOf(width, height)
            Pair(targetW, targetH)
        } else {
            val targetW = maxOf(width, height)
            val targetH = minOf(width, height)
            Pair(targetW, targetH)
        }
    }
}

enum class FrameRateOption(val fps: Int, val title: String, val description: String, val isUltra: Boolean) {
    FPS_120(120, "120 FPS", "Ultra Smooth (Performa Maksimal Gaming & E-Sports)", true),
    FPS_90(90, "90 FPS", "High Refresh Rate (Sangat Mulus)", true),
    FPS_60(60, "60 FPS", "Standar Halus (Ideal untuk YouTube & Streaming)", false),
    FPS_30(30, "30 FPS", "Standar Normal (Hemat Ruang & Baterai)", false),
    FPS_24(24, "24 FPS", "Sinematik (Gaya Film Layar Lebar)", false)
}

enum class BitrateOption(val bps: Int, val label: String) {
    AUTO(0, "Otomatis (Disarankan)"),
    B_50_MBPS(50_000_000, "50 Mbps (Master Studio)"),
    B_32_MBPS(32_000_000, "32 Mbps (Sangat Tinggi)"),
    B_24_MBPS(24_000_000, "24 Mbps (Optimal 120 FPS)"),
    B_16_MBPS(16_000_000, "16 Mbps (Kualitas Tinggi)"),
    B_12_MBPS(12_000_000, "12 Mbps (Standar)"),
    B_8_MBPS(8_000_000, "8 Mbps (Sedang)"),
    B_4_MBPS(4_000_000, "4 Mbps (Hemat Ruang)")
}

enum class VideoCodec(val label: String, val description: String) {
    H264("H.264 / AVC", "Kompatibilitas tertinggi di semua perangkat"),
    HEVC("H.265 / HEVC", "Kompresi efisien, ukuran file lebih kecil")
}

enum class RecordOrientation(val label: String) {
    AUTO("Otomatis (Ikuti Layar)"),
    PORTRAIT("Tegak (Portrait)"),
    LANDSCAPE("Mendatar (Landscape Gaming)")
}

enum class AudioSourceOption(val label: String, val description: String) {
    MIC("Mikrofon Saja", "Rekam suara eksternal dan komentar Anda"),
    INTERNAL_MIC("Internal + Mikrofon", "Suara game/sistem dan mikrofon bersamaan"),
    MUTE("Tanpa Suara (Mute)", "Video tanpa rekaman audio sama sekali")
}

enum class AudioBitrateOption(val kbps: Int, val label: String) {
    KBPS_320(320_000, "320 kbps (Kualitas Studio)"),
    KBPS_256(256_000, "256 kbps (Tinggi)"),
    KBPS_192(192_000, "192 kbps (Standar)"),
    KBPS_128(128_000, "128 kbps (Dasar)")
}

enum class AudioSampleRateOption(val hz: Int, val label: String) {
    HZ_48000(48000, "48.000 Hz (Standar Video Modern)"),
    HZ_44100(44100, "44.100 Hz (Kualitas CD)")
}

data class RecordingConfig(
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val frameRate: FrameRateOption = FrameRateOption.FPS_120,
    val bitrate: BitrateOption = BitrateOption.B_24_MBPS,
    val videoCodec: VideoCodec = VideoCodec.H264,
    val orientation: RecordOrientation = RecordOrientation.AUTO,
    val audioSource: AudioSourceOption = AudioSourceOption.MIC,
    val audioBitrate: AudioBitrateOption = AudioBitrateOption.KBPS_256,
    val audioSampleRate: AudioSampleRateOption = AudioSampleRateOption.HZ_48000,
    val countdownSeconds: Int = 3,
    val shakeToStop: Boolean = true,
    val shakeSensitivity: Float = 14.5f,
    val showFloatingControls: Boolean = true,
    val stopOnScreenOff: Boolean = true,
    val hapticFeedback: Boolean = true
)
