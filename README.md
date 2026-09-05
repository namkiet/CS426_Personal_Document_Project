# Personal Document

An Android app for managing scanned documents and photos in one personal library.

## Group members
- 24125013 — Trinh Vo Nam Kiet
- 24125002 — Doan Tuan Anh
- 24125037 — Tran Khanh Nhan

## Project title
Personal Document

## Demo video
- Link: TBD

## Problem
People collect many documents, receipts, notes, and photos over time, but they are often hard to find later when only a keyword or a vague memory remains.

## Goal
- Keep personal documents in one place
- Search them with keywords or natural-language queries

## Non-goals
- No cloud sync
- No login/account system
- No shared or collaborative features

## Features
- Import or scan documents
- Extract text with on-device ML
- Generate title, category, tags, and description
- Save items in a local database
- Search by text and semantic similarity
- Browse, review, and delete saved documents

## Tech stack
- Kotlin
- Jetpack Compose + Material 3
- Room + DataStore
- ViewModel + Flow
- ML Kit
- MediaPipe embeddings
- Optional Gemini API

## Installation
### Android Studio
1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Install Android SDK if needed.
4. Create a `local.properties` file in the project root:

```properties
sdk.dir=C\:/Users/<YourUser>/AppData/Local/Android/Sdk
GEMINI_API_KEY=your_gemini_api_key_here
```

5. Connect a device or start an emulator.
6. Click Run > Run 'app'.

### Command line
```bash
./gradlew assembleDebug
```

Windows:
```powershell
.\gradlew.bat assembleDebug
```

If `GEMINI_API_KEY` is missing, the app falls back to on-device processing.

## Build instructions
1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Install Android SDK if needed.
4. Create a `local.properties` file in the project root with your Android SDK path.
5. Optionally add `GEMINI_API_KEY` if using the cloud pipeline.
6. Run one of the following commands:
   - `./gradlew assembleDebug`
   - `./gradlew assembleRelease`

## Test account / credentials
- No login system is required for this app.

## Notes
- Source code is in the `app/` module.
- Report PDF is generated in the `report/` folder.
- APK build output should be placed under `app/build/outputs/apk/` or packaged into a release artifact when submitting.