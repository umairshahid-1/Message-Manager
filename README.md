# Message-Manager App

The **Message-Manager App** is an Android application designed to access, manage, send and display SMS messages on a device. It provides a user-friendly interface to view SMS threads, send new messages, and organize messages by sender, date, and SIM slot. The app leverages modern Android development tools such as **Jetpack Compose**, **Room Database**, and **WorkManager** to ensure a smooth and efficient user experience.

---

## Features

- **View SMS Messages**:
  - Display SMS messages in a clean, organized list.
  - Group messages by sender and thread.
  - Show message details such as date, time, and SIM slot used.

- **Send SMS Messages**:
  - Send SMS messages directly from the app.
  - Choose the SIM slot (SIM 1 or SIM 2) for sending messages.

- **Real-Time SMS Sync**:
  - Automatically sync SMS messages with the device's native SMS database.
  - Listen for new incoming messages and update the UI in real time.

- **Background Service**:
  - Use a **Foreground Service** to continuously monitor and sync SMS messages.
  - Schedule periodic syncs using **WorkManager** to ensure data consistency.

- **Permissions Handling**:
  - Request and manage runtime permissions for reading and sending SMS messages.
  - Handle permission denials gracefully with user-friendly prompts.

- **Room Database Integration**:
  - Store SMS messages locally using **Room Database** for offline access.
  - Perform CRUD operations (Create, Read, Update, Delete) on SMS messages.

- **Jetpack Compose UI**:
  - Build a modern, responsive UI using **Jetpack Compose**.
  - Display SMS threads, headers, and message details in a visually appealing way.

---

## Technical Highlights

- **Android Components Used**:
  - **Jetpack Compose**: For building the UI.
  - **Room Database**: For local storage of SMS messages.
  - **WorkManager**: For periodic background syncs.
  - **Foreground Service**: For real-time SMS monitoring.
  - **BroadcastReceiver**: To listen for incoming SMS messages and device boot events.

- **Permissions**:
  - `READ_SMS`, `SEND_SMS`, `READ_PHONE_STATE`, and `RECEIVE_SMS` permissions are required for full functionality.

- **SIM Slot Management**:
  - The app supports dual SIM devices and allows users to send messages from a specific SIM slot.

- **Coroutines**:
  - Use of **Kotlin Coroutines** for asynchronous operations such as database queries and network calls.

- **Modular Code Structure**:
  - Separation of concerns with **ViewModel**, **Repository**, and **DAO** layers for clean and maintainable code.

---

## How It Works

1. **SMS Access**:
   - The app reads SMS messages from the device's native SMS database using `ContentResolver`.
   - Messages are stored in a local **Room Database** for quick access and offline use.

2. **Real-Time Updates**:
   - A **BroadcastReceiver** listens for incoming SMS messages and updates the database in real time.
   - A **Foreground Service** ensures continuous monitoring of SMS messages, even when the app is in the background.

3. **Periodic Sync**:
   - **WorkManager** schedules periodic syncs to ensure the local database stays up-to-date with the device's SMS content.

4. **User Interface**:
   - The UI is built using **Jetpack Compose**, providing a modern and responsive experience.
   - Messages are grouped by sender and thread, with headers showing the date, time, and SIM slot.

5. **Sending SMS**:
   - Users can send SMS messages directly from the app, choosing the SIM slot for sending.

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/umairshahid-1/Message-Manager.git
   
2. Open the project in Android Studio.

3. Build and run the app on an Android device or emulator.

---

## Permissions
The app requires the following permissions:

**READ_SMS**: To read SMS messages from the device.

**SEND_SMS**: To send SMS messages.

**READ_PHONE_STATE**: To determine the SIM slot used for sending messages.

**RECEIVE_SMS**: To listen for incoming SMS messages.

---

## Screenshots

- **Main Page**<br>
![Main Screen](https://i.imgur.com/uU4TcU5.jpeg)

- **Send SMS**<br>
![Send SMS Screen](https://i.imgur.com/E2z5cNR.jpeg)

---

## Dependencies
**Jetpack Compose**: For building the UI.

**Room Database**: For local storage.

**WorkManager**: For background tasks.

**Kotlin Coroutines**: For asynchronous programming.

---

## Future Improvements
- Add support for MMS messages.
- Implement message search and filtering.
- Enhance UI with themes and customization options.
- Improve error handling and user feedback.
