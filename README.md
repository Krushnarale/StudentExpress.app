# StudentExpress — Smart Student Rental & Marketplace Platform

A JavaFX desktop application built for students to rent accommodation, buy/sell items, find roommates, and book campus services.

---

## Features

### Student Portal
- 🏠 **Rent** — Browse and book student accommodation
- 🛒 **Buy & Sell** — Marketplace for textbooks, electronics, cycles, stationery
- 🤝 **Roommates** — Find a roommate or register as one
- 🔧 **Services** — Book campus service providers
- 💬 **AI Chatbot** — Built-in student assistant
- 👤 **Profile** — Manage bookings, orders, saved items, wallet

### Student Seller Portal
- Register as a Seller using the same student account
- Post items for sale with images
- Manage listings and incoming buyer requests
- Switch seamlessly between Student and Seller portals

### Owner Portal
- Manage rental listings and tenant requests

### Service Provider Portal
- List campus services and manage bookings

### Admin Portal
- System-wide management and monitoring

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI Framework | JavaFX 17 |
| Language | Java 17 |
| Backend / Auth | Firebase Admin SDK |
| Database | Cloud Firestore |
| Storage | Cloudinary |
| Build | Maven |

---

## Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- Firebase project with Firestore enabled

### Firebase Configuration

> ⚠️ **Important**: The `serviceAccount.json` is **not included** in this repository for security.  
> You must supply your own Firebase service account key.

1. Go to [Firebase Console](https://console.firebase.google.com/) → Project Settings → Service Accounts
2. Generate a new private key
3. Place the downloaded file at:
   ```
   src/main/resources/firebase/serviceAccount.json
   ```

### Build & Run

```bash
# Compile
mvn clean compile

# Run
mvn javafx:run
```

---

## Project Structure

```
src/main/java/com/core2web/
├── Main.java                    # App entry + navigation
├── MainShell.java               # Shell layout (sidebar, nav)
├── config/                      # Firebase init & seed
├── controller/                  # Business logic controllers
├── dao/                         # Data access objects (Firestore)
├── model/                       # Domain models (User, Room, Product...)
├── repository/                  # DataRepository (in-memory cache + sync)
├── service/                     # External services (Cloudinary)
├── util/                        # Helpers (Theme, Auth, Session, Icons)
└── view/
    ├── authentication/          # Login, Signup, Welcome, Splash pages
    ├── marketplace/             # Buy/Sell, Seller Dashboard
    ├── rent/                    # Rent, Roommate, Owner Dashboard
    ├── services/                # Services, Provider Dashboard
    ├── chatbot/                 # AI Chatbot
    └── component/              # Reusable UI components
```

---

## Login Flow

```
App Start → Splash → Welcome → Role Selection
                                     │
              ┌──────────────────────┼──────────────────────┐
              ▼                      ▼                       ▼
        Student Login          Seller Login           Owner/Provider/Admin Login
              │                      │
              │◄─ "Are you a Seller?" ──►│
              │◄─ "Are you a Student?" ──┤
              │                      │
              ▼                      ▼
       Student Portal         Seller Workspace
```

---

## License

MIT License — for educational and academic use.
