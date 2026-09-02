# Personal Document

An Android app that keeps captured photos in one place and helps users find them using text descriptions.

## Problem

People take many photos but later struggle to find them or remember where they are stored.

## Core features

- Capture or import a photo.
- Use an on-device model to extract information from the photo.
- Store the photo and its metadata locally on the device.
- Find relevant photos using text, for example: `Database lecture about indexing`.

## Current status

The project contains a compile-ready clean architecture implementation with Compose screens for importing, reviewing, browsing, searching, and viewing documents. Imported images are copied to app-internal storage, analyzed on-device, and searchable by text and semantic similarity.

ML Kit provides on-device OCR, while MediaPipe generates local text embeddings and TLDR descriptions. Title, category, and tag generation are rule-based, object detection is currently a placeholder, and document metadata remains in memory until Room persistence is implemented. Direct camera capture is not yet available.

## Architecture

The app uses dependency inversion to keep business rules independent of Android and storage/model frameworks:

```text
Presentation (Compose + ViewModel)
              |
              v
Domain (models + use cases + repository/service contracts)
              ^
              |
Data (local storage + extraction + repository implementations)
```

`AppContainer` is the composition root. It wires concrete data adapters to domain use cases without a dependency-injection framework.

## Project structure

```text
app/src/main/java/com/example/personaldocumentproject/
|-- MainActivity.kt                    # Android entry point
|-- presentation/
|   |-- library/                       # Personal document collection
|   |   |-- components/
|   |   |-- LibraryScreen.kt
|   |   |-- LibraryViewModel.kt
|   |   `-- LibraryUiState.kt
|   |-- capture/                       # Image capture/import
|   |   |-- components/
|   |   |-- CaptureScreen.kt
|   |   |-- CaptureViewModel.kt
|   |   `-- CaptureUiState.kt
|   |-- review/                        # OCR review and save
|   |   |-- components/
|   |   |-- ReviewScreen.kt
|   |   |-- ReviewViewModel.kt
|   |   `-- ReviewUiState.kt
|   |-- search/                        # Text/semantic retrieval
|   |   |-- components/
|   |   |-- SearchScreen.kt
|   |   |-- SearchViewModel.kt
|   |   `-- SearchUiState.kt
|   `-- detail/                        # Saved document details
|       |-- DetailScreen.kt
|       |-- DetailViewModel.kt
|       `-- DetailUiState.kt
|-- domain/
|   |-- model/                         # Document, draft, category, result
|   |-- repository/                    # DocumentRepository contract
|   |-- service/                       # OCR and embedding contracts
|   `-- usecase/                       # Analyze, save, observe, get, search
|-- data/
|   |-- local/
|   |   |-- storage/                   # App-internal image storage
|   |   |-- AppDatabase.kt
|   |   |-- DocumentDao.kt
|   |   |-- DocumentEntity.kt
|   |   `-- converters/
|   |-- ml/
|   |   |-- ocr/                       # ML Kit OCR adapter
|   |   |-- embedding/                 # MediaPipe text embeddings
|   |   |-- metadata/                  # Local metadata and TLDR generation
|   |   `-- objectdetection/           # Object detection placeholder
|   |-- mapper/                        # Entity/domain conversion
|   `-- repository/
|       `-- DefaultDocumentRepository.kt
|-- di/
|   `-- AppContainer.kt                # Dependency composition root
|-- navigation/
|   |-- AppNavGraph.kt
|   `-- Routes.kt
`-- ui/theme/                          # Material 3 theme
```

## Tech stack

- Native Android with Kotlin
- Jetpack Compose and Material 3
- Coroutines and Flow for asynchronous state
- Gradle with Kotlin DSL and a version catalog
- Android SDK 24-37

## Build and test

Use Android Studio's bundled JDK or set `JAVA_HOME`, then run:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

On Windows:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

With an Android device or emulator connected, run the on-device ML tests:

```bash
./gradlew connectedDebugAndroidTest
```

## Goals

- Keep photos and related information in one personal library.
- Help users quickly find photos using natural language.
- Keep all data locally on the device.

## Non-goals

- Sharing photos with others.
- Syncing data across devices.
- Account registration or login.
- Chatting with photos.
