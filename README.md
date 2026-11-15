# RxVaani — Spring Boot

> AI-Powered Clinical Prescription Scribe | Java 17 + Spring Boot 3.2

## 🚀 Quick Start

```bash
# 1. Build
mvn clean install

# 2. Run
mvn spring-boot:run
```

Open: http://localhost:8000

## 📋 Pages
| URL | Page |
|---|---|
| / | Landing Page |
| /consultation | OPD Console |
| /studio | AI Studio |
| /prescription | Prescription Pad |
| /records | Patient Records |
| /settings | Settings |
| /swagger-ui.html | API Docs |

## ⚙️ Config
Set environment variables (optional — works without them in demo mode):
```
GNANI_API_KEY=your_key
GEMINI_API_KEY=your_key
GROQ_API_KEY=your_key
```

## 📁 Structure
```
src/main/java/com/rxvaani/
├── RxVaaniApplication.java
├── config/      CorsConfig, WebClientConfig
├── controller/  PageController, ApiController
├── model/       Prescription, Medicine, Patient, DTOs
├── service/     SttService, ExtractService, PdfService, MedicineService
└── exception/   GlobalExceptionHandler
```
