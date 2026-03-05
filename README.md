# Notes

An Android notes app for two partnered users with shared notes, shared tasks, Google sign-in, and home screen widgets.

## Features

- Partner pairing with a 6-digit code
- Shared notes and shared tasks synced in real time with Firestore
- Separate Notes and Tasks sections in the app
- Document-style notes with dedicated note detail screen
- Email/password and Google sign-in support
- Two home screen widgets:
  - Pending Tasks widget
  - Recent Notes widget

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Firebase Authentication
- Cloud Firestore
- Jetpack Glance (App Widgets)

## Getting Started

1. Clone this repository.
2. Open the project in Android Studio.
3. Create a Firebase project and register Android app ID: `com.example.notes`.
4. Place `google-services.json` inside `app/`.
5. In Firebase Console:
   - Enable Authentication providers:
     - Email/Password
     - Google
   - Create Cloud Firestore database.
6. Build and run the app.

## Build

Use Android Studio, or from terminal:

```bash
./gradlew :app:assembleDebug
```
