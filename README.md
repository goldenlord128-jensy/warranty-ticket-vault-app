# Warranty Vault & Receipt Manager 🛡️receipt

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)
![Room Database](https://img.shields.io/badge/Room-Offline%20First-orange.svg)
![WorkManager](https://img.shields.io/badge/WorkManager-Background%20Sync-yellow.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**Warranty Vault** is a modern, feature-rich Android application built from the ground up using **Jetpack Compose** and **Kotlin**. It helps users securely store, track, and manage product warranties, scan receipts, and receive automated background expiration alerts.

---

## ✨ Key Features

### 🔒 1. Biometric Security Gate
* Integrated with Android BiometricPrompt (`BiometricManager`).
* Secures user data on app startup with fingerprint or face unlock (with device credential fallback).

### 📦 2. Offline-First Room Database
* Local persistence powered by Room Database & Coroutines/Flows.
* Instant data retrieval with reactive UI updates.

### 🔔 3. Automated Background Expiration Notifications
* Powered by **WorkManager** (`CoroutineWorker`) and `NotificationCompat`.
* Periodically checks for warranties expiring within the next 30 days and sends proactive alerts even when the app is closed.

### 🔍 4. Interactive Receipt Zoom Viewer
* Custom touch gesture detector (`detectTransformGestures`) allowing users to pinch-to-zoom (up to 4x) and pan high-resolution receipt images inside a dialog modal.

### 🧾 5. Smart Receipt Parsing & Scanning
* Integrated with ML Kit Text Recognition to extract title, purchase date, and warranty duration directly from captured or imported receipt images.

### 📤 6. Export & Backup
* Export warranty records and receipts to PDF or CSV formats for secure backups.

---

## 🛠️ Tech Stack & Architecture

* **UI Framework:** Jetpack Compose, Material 3, Coil (Async Image Loading).
* **Local Persistence:** Room Database (DAO, Entities, Flows).
* **Background Processing:** Android WorkManager.
* **Security:** AndroidX Biometric.
* **Asynchronous Programming:** Kotlin Coroutines & `StateFlow`.
* **Build System:** Kotlin DSL (`build.gradle.kts`) & Version Catalogs.

---

## 🚀 Getting Started

### Prerequisites
* Android Studio (Ladybug or newer recommended).
* JDK 11 or higher.
* Android device or emulator running **API 26 (Oreo)** or higher.

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/warranty-vault.git
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle sync dependencies.
4. Click **Run (▶️)** to build and deploy to your connected device or emulator.

---

## 📂 Project Structure

```tree
com.example.warrantyvault/
│
├── MainActivity.kt              # App entry point, Biometric prompt & Dashboard UI
├── VaultDatabase.kt             # Room Database configuration
├── VaultItem.kt                 # Entity data model
├── ItemDao.kt                   # Database data access object
├── BiometricHelper.kt           # Biometric authentication wrapper
├── warrantyNotificationWorker.kt# Background WorkManager expiration checker
├── ExpirationHelper.kt          # Warranty date calculation logic
├── ReceiptParser.kt             # ML Kit OCR receipt text extractor
└── ui/theme/                    # Material 3 colors, typography, and themes
```

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check issues page or submit a pull request.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
