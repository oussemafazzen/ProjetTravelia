<div align="center">

# ✈️ ProjetTravelia

### All-in-one Travel Management Desktop Application

*Esprit School of Engineering — Projet Intégré de Développement Logiciel (PIDL) — Travelia · 2025/2026*

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-4B8BBE?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Academic%20Use-blue?style=for-the-badge)](LICENSE)

</div>

---

## 📖 About The Project

**ProjetTravelia** is a fully-featured **Java desktop application** built with **JavaFX**, designed to manage all aspects of a travel agency. It covers the entire customer journey — from browsing accommodations and booking flights, to activity management, payment processing, and intelligent AI-powered recommendations.

This project was developed as an academic group project at **ESPRIT School of Engineering** by the **Travelia** student cohort (2025/2026). Each module was implemented by a dedicated team member, and all modules are fully integrated into a unified platform.

---

## ✨ Key Features

### 🏨 Accommodation Management (`Hébergement`)
- Full CRUD for hotels and rental properties
- Client-facing front office with search & filtering
- **MapBox API** integration to display accommodation locations on an interactive map
- PDF export of accommodation details using **iText 7**
- Statistics dashboard with charts

### 📅 Accommodation Reservations (`Réservation Hébergement`)
- Manage accommodation bookings (dates, client, room type)
- Back-office admin panel with table view and edit dialogs
- Real-time availability checks
- Calendar view for reservations

### ✈️ Flights & Tickets (`Vols & Billets`)
- Search available flights using a **live flight search API**
- View flight results with pricing and duration
- Admin-side billet (ticket) management with CRUD operations
- Payment integration directly from flight results
- Export/print tickets

### 🎟️ General Reservations & Payment (`Réservations & Paiement`)
- Full reservation cycle management for multiple service types
- Multiple payment methods with payment tracking
- Integrated payment history per client
- Statistics on bookings and revenues

### 🎯 Activities & Reviews (`Activités & Avis`)
- Admin CRUD for activity listings (excursions, guided tours, etc.)
- Client activity registration with date and capacity validation
- Review system with star ratings and optional photo uploads
- Photo gallery per activity

### 🤖 AI Recommendations (Weka ML)
- **Destination recommendation engine** trained on client travel history (Weka ARFF format)
- **Activity recommendation engine** using machine learning classification
- Models trained with [Weka 3.8](https://www.cs.waikato.ac.nz/ml/weka/) — `J48` decision tree classifier
- Exporters for ARFF training data generation from the live database

### 🤖 Gemini AI Chatbot
- Built-in chat assistant powered by **Google Gemini API**
- Helps clients discover destinations, activities, and travel tips
- Conversational interface embedded in the front office dashboard

### 🔐 Authentication & User Management (`Authentification & Profil`)
- Classic email/password login with **BCrypt** password hashing
- **Google OAuth 2.0** sign-in support
- **Face Recognition** login using **JavaCV** (OpenCV bindings)
- Password reset via secure email token (Jakarta Mail + Gmail SMTP)
- Role-based access: Client / Administrator

### 📊 Admin Dashboard
- Centralized admin panel with navigation to all modules
- Statistics: reservations per month, revenue charts, top destinations
- Excel export using **Apache POI**
- Security log viewer

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | [Java 17](https://www.oracle.com/java/) (LTS) |
| **UI Framework** | [JavaFX 21.0.1](https://openjfx.io/) (FXML + CSS) |
| **Build Tool** | [Apache Maven 3.x](https://maven.apache.org/) |
| **Database** | [MySQL 8.0](https://www.mysql.com/) |
| **ORM / DB Access** | JDBC (raw SQL with DAO pattern) |
| **AI / ML** | [Weka 3.8](https://www.cs.waikato.ac.nz/ml/weka/) (J48 classifier), [Google Gemini API](https://ai.google.dev/) |
| **Face Recognition** | [JavaCV 1.5.10](https://github.com/bytedeco/javacv) (OpenCV + LBPH) |
| **Authentication** | Google OAuth 2.0 ([google-api-client](https://github.com/googleapis/google-api-java-client)) |
| **Password Security** | [jBCrypt 0.4](https://www.mindrot.org/projects/jBCrypt/) |
| **PDF Generation** | [iText 7.2.5](https://itextpdf.com/) |
| **Excel Export** | [Apache POI 5.2.5](https://poi.apache.org/) |
| **Email** | [Jakarta Mail 2.0.1](https://eclipse-ee4j.github.io/mail/) + Gmail SMTP |
| **JSON Parsing** | [org.json](https://github.com/stleary/JSON-java) + [Gson 2.10.1](https://github.com/google/gson) |
| **Maps** | [MapBox API](https://www.mapbox.com/) (via JavaFX WebView) |
| **Flight Data** | External Flight Search REST API |
| **Testing** | [JUnit 5.10.1](https://junit.org/junit5/) |

---

## 📋 Prerequisites

Before running the project, make sure you have the following installed:

| Tool | Version | Link |
|---|---|---|
| Java JDK | 17 (LTS) | [Download](https://www.oracle.com/java/technologies/downloads/#java17) |
| Apache Maven | 3.8+ | [Download](https://maven.apache.org/download.cgi) |
| MySQL Server | 8.0+ | [Download](https://dev.mysql.com/downloads/mysql/) |
| IntelliJ IDEA | 2023+ (recommended) | [Download](https://www.jetbrains.com/idea/) |
| Git | Latest | [Download](https://git-scm.com/) |

> **JavaFX** is handled automatically by Maven — no separate installation needed.

---

## 🚀 Installation & Setup

### Step 1 — Clone the repository

```bash
git clone https://github.com/oussemafazzen/ProjetTravelia.git
cd ProjetTravelia
```

### Step 2 — Set up the database

1. Open **MySQL Workbench** or any MySQL client.
2. Create a new database:
   ```sql
   CREATE DATABASE travelia;
   ```
3. Import the provided SQL schema:
   ```bash
   mysql -u root -p travelia < database/travelia.sql
   ```

### Step 3 — Configure the database connection

Open `src/main/java/utils/MyDatabase.java` and update the connection details:

```java
private static final String URL = "jdbc:mysql://localhost:3306/travelia";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### Step 4 — Configure API Keys

Open or create a configuration file (see `src/main/resources/config.properties`) and add your API keys:

```properties
# Google Gemini AI Chatbot
GEMINI_API_KEY=your_gemini_api_key

# Google OAuth 2.0
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# MapBox (for interactive maps)
MAPBOX_TOKEN=your_mapbox_access_token

# Gmail SMTP (for password reset emails)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

> 💡 **Where to get API keys:**
> - Gemini API: [Google AI Studio](https://aistudio.google.com/)
> - Google OAuth: [Google Cloud Console](https://console.cloud.google.com/)
> - MapBox: [mapbox.com](https://www.mapbox.com/)
> - Gmail App Password: [Google Account → Security → App Passwords](https://myaccount.google.com/security)

### Step 5 — Install dependencies

```bash
mvn install
```

### Step 6 — Run the application

```bash
mvn javafx:run
```

Or run directly from **IntelliJ IDEA**: open the project, let Maven sync, then run `MainFX` class.

---

## 🧪 Running Tests

```bash
# Run all JUnit 5 tests
mvn test
```

Test classes are located in `src/test/java/`.

---

## 📁 Project Structure

```
ProjetTravelia/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── ai/                         # Weka ML recommendation engines
│   │   │   │   ├── ActivityRecommendationAI.java
│   │   │   │   ├── TraveliaRecommendationAI.java
│   │   │   │   └── ...
│   │   │   ├── controllers/                # JavaFX FXML controllers
│   │   │   │   ├── HebergementController.java
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── ...
│   │   │   ├── models/                     # Entity / data model classes
│   │   │   │   ├── Hebergement.java
│   │   │   │   ├── Client.java
│   │   │   │   ├── Reservation.java
│   │   │   │   └── ...
│   │   │   ├── services/                   # Business logic & DB access
│   │   │   │   ├── HebergementService.java
│   │   │   │   ├── GeminiChatService.java
│   │   │   │   ├── FaceRecognitionService.java
│   │   │   │   └── ...
│   │   │   ├── interfaces/                 # Service interfaces (CRUD contracts)
│   │   │   ├── utils/                      # Utilities (DB connection, helpers)
│   │   │   └── test/
│   │   │       └── MainFX.java             # JavaFX Application entry point
│   │   └── resources/
│   │       ├── *.fxml                      # FXML UI layout files
│   │       ├── *.css                       # JavaFX stylesheets
│   │       ├── *.png / *.jpg              # Image assets
│   │       └── *.arff                      # Weka ML training data
│   └── test/
│       └── java/                           # JUnit 5 test suites
├── pom.xml                                 # Maven build configuration
└── README.md
```

---

## 👥 Team

This project was built collaboratively by the **Travelia** student team at **Esprit School of Engineering** as part of the PIDL integrated project module.

| Name | Role / Module |
|---|---|
| **Oussema Fazzen** | Hébergement & Réservation Hébergement |
| **Syrine BenRjeb** | Vols & Billets |
| **Syrine BenRjeb** | Réservations & Paiement |
| **Syrine Boukhit** | Authentification & Profil Utilisateur |
| **Tout le groupe** | Activités & Avis |

---

## 📞 Contact

Have a question, found a bug, or want to collaborate?

| Channel | Link |
|---|---|
| **GitHub Issues** | [Open an issue](https://github.com/oussemafazzen/ProjetTravelia/issues) |
| **GitHub Repository** | [github.com/oussemafazzen/ProjetTravelia](https://github.com/oussemafazzen/ProjetTravelia) |
| **Project Lead** | [@oussemafazzen](https://github.com/oussemafazzen) |

---

## 📄 License

This project is developed for **academic purposes only** as part of the ESPRIT School of Engineering curriculum (2025/2026). All rights reserved by the contributing student team.

---

<div align="center">

Made with ❤️ by the **Travelia Team** · ESPRIT School of Engineering · 2025/2026

</div>
