gi# EduFlow

A mobile Android application (app module in `app/`) that helps educational institutes manage common tasks such as attendance, assignments, study materials and announcements. This repository is an Android project built with Gradle and contains an `app/` module. The project includes `app/google-services.json`, so Firebase services are likely configured in the app.

This README has been updated to reflect the actual repository contents (Android / Gradle) and to provide clear setup, build and contribution instructions.

---

## Quick summary

- Platform: Android (Kotlin / Java)
- Build system: Gradle (wrapper included)
- Module: `app/` (Android application)
- Firebase: `app/google-services.json` is present (Firebase services such as Analytics/Authentication/FCM may be used)

---

## Requirements

- JDK 11 or newer (match project's gradle JVM if different)
- Android SDK (platform and build tools recommended by Android Studio)
- Android Studio (recommended) or command-line Gradle
- A connected Android device or emulator for instrumentation tests

---

## Get the code

1. Clone the repository and open it in Android Studio, or use the command line.

```cmd
git clone <repository-url>
cd "D:\CMW\01 Projects\Android Studio\EduFlow"
```

2. Open the project in Android Studio (File → Open) and let it sync Gradle and download dependencies.

Notes:
- Keep the Android SDK and build tools versions compatible with the project (Android Studio normally suggests what to install when you open the project).
- If you use a command line, use the included Gradle wrapper (`gradlew` / `gradlew.bat`).

---

## Build (command line - Windows cmd)

- Clean and assemble a debug build:

```cmd
.\gradlew.bat clean assembleDebug
```

- Build release (if signing configs are set up):

```cmd
.\gradlew.bat assembleRelease
```

---

## Run on a device (manual install)

After building a debug APK, install it to a connected device or emulator:

```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Or run and debug directly from Android Studio by selecting a device and pressing Run.

---

## Tests & Quality

- Run unit tests (JVM):

```cmd
.\gradlew.bat test
```

- Run instrumentation (Android) tests on a connected device:

```cmd
.\gradlew.bat connectedAndroidTest
```

- Run lint checks:

```cmd
.\gradlew.bat lint
```

If the project uses additional analyzers (detekt, ktlint, etc.), run those tasks if configured.

---

## Project structure (top-level highlights)

- `app/` — Android application module (source under `app/src/main/java` and/or `app/src/main/kotlin`)
- `app/google-services.json` — Firebase configuration for the Android app
- `build.gradle.kts`, `settings.gradle.kts` — Gradle Kotlin DSL build files
- `gradlew`, `gradlew.bat` — Gradle wrapper (use these to build reproducibly)
- `local.properties` — local Android SDK path (not checked into VCS by default)

---

## Notes & assumptions

- I kept descriptions conservative and accurate to files present in the repo. The previous README referenced Flutter/Node/Mongo/etc.; those do not appear to be part of this repository.
- Because `app/google-services.json` exists, Firebase is likely used by this project (Analytics, Auth, Cloud Messaging, Storage, etc.). No other backend configuration files were found at the repository root.
- If your project depends on any backend services or a server component, consider adding a `server/` folder or a link to the backend repository and document environment variables and startup steps.

---

## Contributing

- Create a branch for your changes: `git checkout -b feature/your-feature`
- Keep commits focused and include tests where appropriate
- Open a pull request describing the change and the motivation

If you maintain contribution guidelines (CODE_OF_CONDUCT.md, CONTRIBUTING.md), add or link them.

---

## License

If you want this repository to be open source, add a `LICENSE` file (for example MIT, Apache 2.0). If no license is present, treat the repository as proprietary.

---

## Contact

Project owner / contact: chandula wijesekara

Project link (if available): update this to the repository URL
