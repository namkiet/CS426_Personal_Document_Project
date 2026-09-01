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

The project contains a compile-ready clean architecture scaffold and an initial Compose library screen. A user can import an image, create a temporary document entry, and filter entries by text.

The local and ML packages expose the intended Room, ML Kit OCR, and local embedding boundaries. Their current implementations remain lightweight in-memory/placeholder adapters. Room annotations and persistence, the ML Kit runtime, direct camera capture, and image rendering are the next infrastructure tasks.

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

`AppContainer` is the composition root. It wires concrete data adapters to domain use cases without introducing a dependency-injection framework during the scaffolding stage.

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
|   `-- search/                        # Text/semantic retrieval
|       |-- components/
|       |-- SearchScreen.kt
|       |-- SearchViewModel.kt
|       `-- SearchUiState.kt
|-- domain/
|   |-- model/                         # Document, draft, category, result
|   |-- repository/                    # DocumentRepository contract
|   |-- service/                       # OCR and embedding contracts
|   `-- usecase/                       # Analyze, save, observe, get, search
|-- data/
|   |-- local/
|   |   |-- AppDatabase.kt
|   |   |-- DocumentDao.kt
|   |   |-- DocumentEntity.kt
|   |   `-- converters/
|   |-- ml/
|   |   |-- ocr/                       # ML Kit OCR adapter
|   |   `-- embedding/                 # Local embedding adapter
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

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
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
