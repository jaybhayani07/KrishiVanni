# KrishiVani (કૃષિવાણી) 🌾

KrishiVani is a modern, full-stack, AI-powered agricultural advisory platform designed specifically for farmers in Gujarat. It aggregates real-time agricultural data, live market prices, and AI-driven weather analysis to provide actionable farming tips.

## 🚀 Features

- **Real-Time Market Prices (Mandi Bhav):** Integrates with the `Data.gov.in` (Agmarknet) API to fetch live APMC market prices for crops in various districts of Gujarat.
- **Satellite Crop Monitoring (NDVI):** Automatically provisions land polygons using the `Agromonitoring API` and fetches real-time satellite imagery to calculate NDVI (Normalized Difference Vegetation Index) for remote crop health monitoring.
- **AI-Powered YouTube Weather Consensus:** Uses `Llama-3.3-70b-versatile` (via Groq API) and Python to fetch the latest video transcripts from popular Gujarati agriculture YouTube channels (Paresh Goswami, Ambalal Patel, Jagat Tat), analyzing them to generate a synthesized "Expert Weather Consensus" and highly personalized, actionable 4-5 line farming tips.
- **Hyper-Local Weather Forecasting:** Uses the `OpenMeteo API` to provide 7-day precise temperature and weather forecasts based on the farmer's GPS coordinates or selected district.
- **Beautiful Android App:** Built natively using **Jetpack Compose** (Kotlin), featuring a clean, Gujarati-localized User Interface, smooth animations, and dynamic progress tracking.
- **Robust Backend Infrastructure:** A high-performance **Spring Boot 3** backend connected to a scalable **PostgreSQL** database (hosted on Neon.tech), managing background synchronization, data persistence, and AI script orchestration.

## 🏗️ Architecture & Tech Stack

### 📱 Frontend (Android)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose, Material 3
- **Networking:** Retrofit2, Gson
- **Architecture:** MVVM (Model-View-ViewModel) with StateFlow
- **Features:** Live GPS polling, Interactive Tabs (Dashboard & AI Advisory), Dynamic UI States.

### ⚙️ Backend (Spring Boot)
- **Language:** Java 21
- **Framework:** Spring Boot 3.2.5
- **Database:** PostgreSQL (Neon) with Spring Data JPA & Hibernate
- **Build Tool:** Gradle
- **Security:** Environment Variables (`spring-dotenv`) for protecting API keys.
- **APIs Integrated:** Agromonitoring, OpenMeteo, Data.gov.in (Agmarknet).

### 🤖 AI Processing (Python)
- **Language:** Python 3
- **Libraries:** `youtube-transcript-api`, `requests`, `python-dotenv`
- **LLM Engine:** Groq API (Llama 3.3 70B)
- **Task:** Fetches live transcripts, analyzes agricultural context, generates localized Gujarati responses, and communicates live execution status to the Spring Boot backend via JSON.

## 🛠️ Setup & Installation

### Prerequisites
- Android Studio (for Frontend)
- Java 21 & Gradle (for Backend)
- Python 3.10+ (for AI Scripts)
- PostgreSQL Database (Neon DB or local)

### 1. Python AI Environment Setup
```bash
cd BackEnd/youtube_analyzer
pip install -r requirements.txt # Ensure python-dotenv, youtube-transcript-api, requests are installed
cp .env.example .env
# Edit .env and add your GROQ_API_KEY
```

### 2. Spring Boot Backend Setup
```bash
cd BackEnd/KrishiVani
cp .env.example .env
# Edit .env to add DB credentials and API Keys (Agromonitoring, Data.gov.in)
./gradlew bootRun
```
*The backend will automatically start polling APMC market prices and handling requests at `http://localhost:2503/`.*

### 3. Android App Setup
- Open `Frontend/app` in Android Studio.
- Sync Gradle files.
- The app defaults to connecting to the local backend on the emulator via `http://10.0.2.2:2503/`.
- Run the app on an emulator or physical device.

## 🤝 Contribution
Feel free to open issues, submit Pull Requests, and suggest new features to help make farming smarter and more accessible.
