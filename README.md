# Currency Trend – Android App

An Android app that lets you pick **any two currencies** and view their exchange-rate trend using the free [Frankfurter API](https://api.frankfurter.dev/) (no API key required).

---

## Features

| Feature | Detail |
|---|---|
| Currency picker | Full dropdown list of all currencies supported by Frankfurter (~30) |
| Period selector | 7 · 14 · 30 · 60 · 90 days |
| Animated line chart | Filled cubic-bezier curve, pinch-zoom supported |
| Stats bar | Latest rate · % change (green ▲ / red ▼) · Min · Max |
| Data table | Date-rate rows, newest first, alternating row colours |

---

## How to Open & Run

### Requirements
- **Android Studio Giraffe** (2022.3) or newer
- Android device or emulator running **API 26+** (Android 8.0)
- Internet connection (the app calls `api.frankfurter.dev`)

### Steps

```
1. Open Android Studio
2. File → Open  →  select the  CurrencyTrendApp  folder
3. Wait for Gradle sync to finish (first run downloads dependencies)
4. Click  Run ▶  (Shift+F10) to build and deploy
```

Android Studio will automatically download the Gradle wrapper if it is missing.

---

## Project Structure

```
CurrencyTrendApp/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/currencytrend/app/
│   │   ├── api/
│   │   │   ├── FrankfurterApi.kt       ← Retrofit interface
│   │   │   └── RetrofitClient.kt       ← OkHttp + Retrofit singleton
│   │   ├── models/
│   │   │   └── Models.kt               ← CurrencyRate, CurrencyOption, response DTOs
│   │   ├── CurrencyRepository.kt       ← Data layer (calls API, maps results)
│   │   ├── CurrencyViewModel.kt        ← MVVM ViewModel, LiveData
│   │   ├── RateAdapter.kt              ← RecyclerView adapter (DiffUtil)
│   │   └── MainActivity.kt             ← UI, chart rendering, stat cards
│   └── res/
│       ├── layout/activity_main.xml    ← Full scrollable layout
│       ├── layout/item_rate.xml        ← Row layout for table
│       └── values/{strings, colors, themes}.xml
├── app/build.gradle
├── build.gradle
└── settings.gradle
```

---

## Tech Stack

| Library | Purpose |
|---|---|
| Kotlin + Coroutines | Language + async |
| Retrofit 2 + Gson | REST API client |
| OkHttp logging interceptor | HTTP debugging |
| MPAndroidChart v3.1 | Line chart |
| Material Components 1.11 | Cards, buttons, spinners |
| ViewModel + LiveData | MVVM |

---

## API

`https://api.frankfurter.dev/` — free, open, no registration.

Example query for 30-day CAD → INR:
```
GET https://api.frankfurter.dev/2024-05-01..2024-05-31?base=CAD&symbols=INR
```
