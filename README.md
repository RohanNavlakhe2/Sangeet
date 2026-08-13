# 🎵 Sangeet (Music Player)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java%20%2F%20Kotlin-orange.svg?logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Sangeet** is a lightweight, intuitive Android music player application. It is designed to provide a seamless listening experience, focusing on local audio management and straightforward media playback controls.

---

## 🌟 Key Features

* 🎧 **Audio Playback:** Native media playback support for local audio files.
* 📋 **Playlist Management:** Simple interface for browsing and selecting your music library.
* ⏯️ **Media Controls:** Standard controls (Play, Pause, Next, Previous) with a clean, responsive UI.
* 📱 **Optimized UI:** Designed with a focus on usability and standard Android design patterns.

---

## 🛠️ Tech Stack

* **Language:** Java / Kotlin
* **Media Framework:** Android `MediaPlayer` API
* **Architecture:** Pattern-based separation for UI and background logic
* **UI Components:** RecyclerView (for song lists), Layout XMLs
* **Min SDK Version:** Android 5.0 (API Level 21) or higher

---

## 📂 Repository Structure

```text
Sangeet/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/sangeet/
│   │   │   │   ├── adapters/      # RecyclerView adapters for song lists
│   │   │   │   ├── models/        # Data classes for Song/Track
│   │   │   │   └── ui/            # Activities, Fragments, and Player controls
│   │   │   └── res/               # Layout XMLs, drawables, and UI assets
│   └── build.gradle
└── README.md
