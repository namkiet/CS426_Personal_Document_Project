# Personal Document

A local Android app for managing scanned documents and photos in one personal library.

## Problem

People collect many documents, receipts, notes, and photos over time, but they are often hard to find later when only a keyword or a vague memory remains.

## Goal

- Keep personal documents in one place
- Search them with keywords or natural-language queries
- Process everything locally on-device

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