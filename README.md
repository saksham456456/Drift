# Drift 🌊

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

> *Thoughts float. You catch the ones that resonate.*

**Drift** is a genuinely innovative, privacy-first anonymous social app for Android. It's not a chat app — it's an anonymous thought network built around a single, beautiful idea: anonymous thoughts float through a dark ambient ocean, and you catch the ones that speak to you.

## ✨ Core Concepts

| Concept | Description |
|---------|-------------|
| **Drifts** | Anonymous thoughts that float through a shared ocean. Release your own or catch someone else's. |
| **Pockets** | Ephemeral 1-on-1 conversations created when you catch a Drift. They have an oxygen ring — when conversation dies, the Pocket dissolves and messages are erased forever. |
| **Shifting Identity** | Your anonymous name and avatar evolve based on your karma. Kind users glow warm gold. Toxic users fade cold blue. |
| **Constellations** | Can't add friends. But if a conversation changes you, form a star ✦ — a one-time-use beacon to find each other once more. |

## 🏗 Architecture
- **100% Kotlin** with Jetpack Compose
- **Clean Architecture:** Domain → Data → Presentation
- **MVI State Management** with Kotlin StateFlow
- **Dependency Injection** via Dagger Hilt
- **Firebase** Anonymous Auth + Realtime Database

## 🚀 Getting Started
1. Clone: `git clone https://github.com/saksham456456/anonymous-chat-app.git`
2. Open in **Android Studio**
3. Create a Firebase project, enable Anonymous Auth & Realtime DB
4. Place `google-services.json` in `app/`
5. Build and run!

## 🛡 License
MIT License — see [LICENSE](LICENSE)
