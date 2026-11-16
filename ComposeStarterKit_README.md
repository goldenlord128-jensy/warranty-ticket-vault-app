# ComposeFlow: Jetpack Compose Android Starter Kit 🚀

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)
![Room Database](https://img.shields.io/badge/Room-Offline%20First-orange.svg)
![WorkManager](https://img.shields.io/badge/WorkManager-Background%20Tasks-yellow.svg)
![Biometric](https://img.shields.io/badge/Security-Biometric%20Auth-red.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**ComposeFlow** is a production-ready, highly polished **Android Starter Kit & Boilerplate** built with **Jetpack Compose** and **Kotlin**. It is designed to save developers 10+ hours of boilerplate setup for secure, offline-first Android applications.

---

## ✨ Key Features Included

### 🔒 1. Biometric Authentication Gate
* Pre-configured `BiometricHelper` enforcing fingerprint / face unlock (with device credential fallback) before accessing the app dashboard.

### 📦 2. Offline-First Room Database
* Complete database setup (`AppDatabase`, `ItemDao`, and `Item` entity) with reactive **Flows** and built-in **Real-Time Search**.

### 🔔 3. Background WorkManager & Notifications
* Pre-configured `CoroutineWorker` (`StarterNotificationWorker`) for handling background sync, reminders, and custom push notifications.

### 🌓 4. Dynamic Theme Support (Dark / Light Mode)
* Built-in Material 3 color schemes with a fully functional Dark/Light mode toggle switch in the settings dialog.

### 🔍 5. Responsive Dashboard & Forms
* Clean LazyColumn item lists, search bar filtering, and Material 3 alert dialogs for adding new records.

---

## 🛠️ Tech Stack & Architecture

* **UI Framework:** Jetpack Compose, Material 3, Material Icons Extended.
* **Local Persistence:** Room Database (Coroutines & StateFlow).
* **Security:** AndroidX BiometricPrompt.
* **Background Processing:** Android WorkManager.
* **Build System:** Kotlin DSL (`build.gradle.kts`) & Version Catalogs.

---

## 🚀 Getting Started

### Prerequisites
* Android Studio (Ladybug or newer recommended).
* JDK 11 or higher.
* Android device or emulator running **API 26 (Oreo)** or higher.

### Installation & Customization
1. Unzip and open the project folder in **Android Studio**.
2. Update the package name in your `build.gradle.kts` (`namespace`) and top of your Kotlin files to match your brand (`com.yourname.app`).
3. Let Gradle sync dependencies.
4. Click **Run (▶️)** to build and test on your device!

---

## 📂 Project Structure

```tree
com.example.starterkit/
│
├── MainActivity.kt              # Biometric entry point, Search Dashboard & Settings UI
├── data/
│   ├── Item.kt                  # Room Entity data model
│   ├── ItemDao.kt               # Database queries (CRUD & Real-time search)
│   └── AppDatabase.kt           # Singleton Room database instance
├── utils/
│   └── BiometricHelper.kt       # BiometricPrompt authentication wrapper
└── worker/
    └── StarterNotificationWorker.kt # Background task & notification handler
```

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
