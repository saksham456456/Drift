# Anonymous Chat 🎭

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

A fully open-source, privacy-first Android chat application built with modern Kotlin and Jetpack Compose. No phone numbers, no emails, no real names. Just secure, ephemeral connections.

## ✨ Features
*   **Zero-Knowledge Onboarding:** Instant identity generation (e.g., "Crimson Viper").
*   **End-to-End Privacy:** Built with offline-first local persistence and ephemeral state.
*   **Modern Android Architecture:** 100% Kotlin, Jetpack Compose, Coroutines/Flow, and Dagger Hilt.
*   **Clean Architecture:** Strict separation of Domain, Data, and Presentation layers.

## 🏗 Architecture
This app follows the official Google app architecture guidelines:
*   **UI Layer:** Jetpack Compose + MVI State Management (Unidirectional Data Flow).
*   **Domain Layer:** Pure Kotlin UseCases encapsulating core business rules.
*   **Data Layer:** Room Database (Offline-First) + Firebase Realtime DB.

## 🚀 Getting Started
1. Clone the repository: `git clone https://github.com/saksham456456/anonymous-chat-app.git`
2. Open the project in **Android Studio**.
3. Create a Firebase project, enable Anonymous Auth and Realtime DB.
4. Place your `google-services.json` in the `app/` directory.
5. Build and run!

## 🛡 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
