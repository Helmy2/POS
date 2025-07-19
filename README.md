# Offline-First POS/ERP Application

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-7F52FF?style=for-the-badge&logo=kotlin)
![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-4285F4?style=for-the-badge&logo=jetpackcompose)
![Supabase](https://img.shields.io/badge/Supabase-Backend-3ECF8E?style=for-the-badge&logo=supabase)
![Android](https://img.shields.io/badge/Android-Target-3DDC84?style=for-the-badge&logo=android)
![Desktop](https://img.shields.io/badge/Desktop-Target-000000?style=for-the-badge&logo=windows)
![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

A robust, offline-first Point of Sale (POS) and Enterprise Resource Planning (ERP) application built
with Kotlin Multiplatform (KMP) to target **Android** and **Desktop** platforms from a single,
shared codebase.

## ✨ Core Objective

The primary goal is to deliver a reliable POS/ERP system where primary transactional functions (
e.g., creating sales invoices, managing purchases) work seamlessly without an internet connection.
Data is stored locally in a Room database and intelligently synchronized with a Supabase backend
whenever a connection is available.

---

## 🚀 Key Features

* **Offline-First Functionality**: Create, update, and manage sales, purchases, and returns entirely
  offline.
* **Cross-Platform**: A single Kotlin codebase for both Android and Desktop (Windows, macOS, Linux).
* **Centralized Backend**: Powered by Supabase for database, authentication, real-time updates, and
  serverless functions.
* **Role-Based Access Control**: Secure data access with PostgreSQL's Row-Level Security (RLS),
  restricting data visibility based on user roles (Admin vs. Employee).
* **Real-time & Delta Sync**: Efficiently synchronizes data by fetching only what has changed and
  listening for live updates.
* **Modern Tech Stack**: Built with Jetpack Compose, Coroutines & Flow, Koin for DI, and a clean,
  modular MVI architecture.
* **Transactional Integrity**: Utilizes secure, server-side RPC functions for critical operations
  like stock transfer approvals.
* **Push Notifications**: Designed architecture for sending and receiving push notifications for
  important events.

---

## 🏗️ Architecture

This project is built upon a solid foundation of modern software architecture principles to ensure
it is scalable, maintainable, and testable.

* **Clean Architecture**: Dependencies point inwards from the Presentation layer to the Domain
  layer, creating a separation of concerns between the UI, business logic, and data.
* **Model-View-Intent (MVI)**: The presentation layer follows a strict, unidirectional data flow
  pattern. Each feature is composed of a `Contract`, `ViewModel`, `Reducer`, and a stateless
  `Screen`.
* **Modularization**: The code is organized by feature, promoting high cohesion and low coupling
  between different parts of the application.

For a more in-depth explanation of the architecture, please see
the [ARCHITECTURE.md](ARCHITECTURE.md) file.

---

## 🛠️ Technology Stack & Libraries

* **Platform**: Kotlin Multiplatform (KMP)
* **UI**: Jetpack Compose for Multiplatform
* **Backend**: Supabase
    * **Database**: PostgreSQL
    * **Authentication**: Supabase GoTrue
    * **Real-time**: Supabase Realtime
    * **Serverless Functions**: Supabase Edge Functions (Deno/TypeScript)
* **Client-Side Database**: Room
* **Networking**: `supabase-kt` (Ktor-based client)
* **Dependency Injection**: Koin
* **Asynchronous Programming**: Kotlin Coroutines & Flow
* **Date/Time**: `kotlinx-datetime`
* **Push Notifications**: `KMPNotifier` with Firebase Cloud Messaging (FCM)

---

## 🔄 Data Synchronization Strategy

The application employs a sophisticated, multi-layered approach to data synchronization:

1. **Sync on Open (Pull Phase)**: On the first launch or after login, the app performs an initial
   pull to fetch all necessary read-only data (Products, Categories, etc.) and populates the local
   Room database.
2. **Delta Sync (Incremental Pull)**: For subsequent syncs, the app requests only the records that
   have changed since the last successful sync timestamp, minimizing data transfer.
3. **Pushing Offline Data (Push Phase)**: When the app comes online, it gathers all locally created
   or modified records (marked as "dirty") and pushes them to the Supabase backend. This uses a "
   client-wins" strategy.
4. **Real-time Listeners**: For an even more responsive experience, the app subscribes to Supabase
   Realtime channels to receive live data updates while in the foreground.

---

## 📈 Project Status (As of July 19, 2025)

### ✅ Completed

* **Full Architecture Definition**: Clean Architecture + MVI blueprint is established and
  implemented.
* **Supabase Backend**: Production-grade PostgreSQL schema with RLS policies is complete.
* **Authentication Flow**: Full user login, logout, and session management.
* **Initial Data Sync**: The "Sync on Open" pull phase is fully implemented.
* **Core Feature Refactoring**: All primary screens (Login, Dashboard, Settings, CRUD screens) have
  been refactored to the final MVI blueprint.
* **Stock Transfer Workflow**: Database schema and secure RPC function for approvals are complete.
* **Global Utilities**: Centralized `NavigationController`, `SnackbarController`, and other core
  utilities are in place.

### 🚀 Future Roadmap

* **Implement "Push" Synchronization**: Build the repository logic to push offline-created ("dirty")
  records to Supabase.
* **Activate Real-time Listeners**: Implement Supabase Realtime listeners for live data updates.
* **Background Sync**: Use `WorkManager` (or a KMP equivalent) for periodic background data syncing.
* **Finalize Push Notifications**: Implement the client-side listeners to handle incoming
  notifications.
* **UI Polish**: Conduct a final pass on the UI for all features to ensure a polished user
  experience.

---

## 🏁 Getting Started

To get the project up and running, follow these steps:

1. **Clone the repository:**

2. **Supabase Configuration:**
    * You will need a Supabase project.
    * Create a `local.properties` file in the root directory of the project.
    * Add your Supabase URL and public anonymous key to this file:
        ```properties
        supabaseUrl="YOUR_SUPABASE_URL"
        supabaseKey="YOUR_SUPABASE_ANON_KEY"
        serviceRole="YOUR_SUPABASE_SERVICE_ROLE_KEY"
        ```

3. **Run the Application:**
    * **Android**: Open the project in Android Studio and run the `app` configuration on an emulator
      or a physical device.
    * **Desktop**: Use the appropriate Gradle task in the terminal or IntelliJ IDEA to run the
      desktop application:
        ```bash
        ./gradlew :app:run
        ```

---
