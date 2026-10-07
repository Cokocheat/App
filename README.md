# RecStudio 120 FPS 🎬⚡

**Aplikasi Perekam Layar Android Modern dengan Kontrol Penuh & Dukungan Hingga 120 FPS Ultra-Smooth.**

---

## 📥 Download File APK Langsung

File APK sudah di-compile dan siap diinstal langsung ke perangkat Android:
* 👉 **[Download RecStudio_120FPS.apk](./release/RecStudio_120FPS.apk)** *(Tersedia di folder `release/RecStudio_120FPS.apk`)*
* Atau unduh file APK terbaru melalui tab **Releases** / **Actions** di repositori GitHub ini.

---

## ✨ Fitur Utama

- 🚀 **Dukungan 120 FPS Ultra-Smooth**:
  - Pilihan frame rate: 120 FPS (Gaming & E-Sports), 90 FPS, 60 FPS, 30 FPS, dan 24 FPS.
  - Deteksi otomatis refresh rate layar fisik (60Hz / 90Hz / 120Hz).
- 📺 **Resolusi Fleksibel**:
  - Mendukung 4K UHD (2160p), 2K QHD (1440p), Full HD (1080p), HD (720p), SD (480p), dan Resolusi Asli Layar (Native).
- ⚡ **Bitrate Kualitas Studio**:
  - Pilihan bitrate hingga 50 Mbps Master, 32 Mbps, 24 Mbps (Optimal untuk 120 FPS), 16 Mbps, dan 8 Mbps.
- 🎙️ **Kontrol Audio Lengkap**:
  - Mikrofon, Internal Sistem + Mikrofon, atau Tanpa Suara (Mute).
  - Bitrate audio hingga 320 kbps Studio & sample rate 48.000 Hz.
- 📳 **Fitur Pintar**:
  - **Hitung Mundur (Countdown)**: Animasi 3s, 5s, 10s dengan getaran haptik responsif.
  - **Goyang HP untuk Berhenti (Shake to Stop)**: Deteksi akselerometer untuk menghentikan rekaman tanpa membuka aplikasi.
  - **Hentikan saat Layar Mati**: Menekan tombol power otomatis menyimpan video.
  - **Widget Mengambang (Floating Controls)**: Kontrol melayang interaktif yang dapat digeser.
- 🎞️ **Galeri Rekaman & Pemutar Slow-Mo**:
  - Pemutar video bawaan dengan fitur analisis kecepatan *Slow-Motion* (0.25x, 0.5x, 1.0x, 1.5x, 2.0x) untuk menguji kehalusan rekaman 120 FPS.
  - Berbagi video langsung, ubah nama, dan hapus video.

---

## 🛠️ Teknologi & Arsitektur

- **Bahasa**: Kotlin (100%)
- **UI Framework**: Jetpack Compose + Material Design 3 (Studio Dark Theme)
- **Engine Perekam**: Android `MediaProjection`, `VirtualDisplay`, `MediaRecorder` dengan Foreground Service
- **Arsitektur**: MVVM (Model-View-ViewModel) + Kotlin Coroutines & StateFlow

---

## 💻 Cara Menjalankan di Android Studio

1. Clone repositori ini:
   ```bash
   git clone https://github.com/USERNAME/recstudio-120fps.git
   ```
2. Buka folder proyek di **Android Studio**.
3. Sinkronkan Gradle.
4. Klik **Run** atau build APK via terminal:
   ```bash
   gradle assembleDebug
   ```
   File APK akan dihasilkan di `app/build/outputs/apk/debug/app-debug.apk`.
