# My-Security-Project

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-blue.svg)](https://spring.io/projects/spring-security)
[![License: MIT](https://img.shields.io/badge/License-MIT-amber.svg)](https://opensource.org/licenses/MIT)

A production-ready **Java Spring Boot 3 & Spring Security** enterprise project with Gmail authentication, BCrypt password hashing, defense-in-depth HTTP headers, Web CSV batch processing, and an interactive **Artistic UPI QR Studio** frontend.

---

## 📂 Project Structure

```text
My-security-project/
│
├── .gitignore
├── pom.xml
├── README.md
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── demo/
        │               ├── DemoApplication.java         <-- Spring Boot Main Runner
        │               ├── config/
        │               │   └── SecurityConfig.java      <-- Spring Security 6 Config
        │               └── controller/
        │                   └── AuthController.java      <-- Gmail Login/Register & CSV API
        └── resources/
            ├── application.properties                   <-- App Config & Port 8080
            └── static/
                └── index.html                           <-- Frontend Studio & Standee Engine
```

---

## 🚀 Features

1. **Spring Security 6 Defense-in-Depth:**
   - BCrypt password hashing with 12 rounds.
   - HSTS, X-Content-Type-Options: nosniff, and Referrer-Policy headers.
   - Public access to frontend (`/`) and protected API boundaries.
2. **Gmail Authentication & Registration:**
   - Registration strictly validates Gmail IDs (`@gmail.com`).
   - Secure login and user session verification endpoints.
3. **Web CSV Processing:**
   - `/api/csv/export-users`: Exports registered user registry as CSV.
   - `/api/csv/template`: Downloads the standard NPCI UPI batch template.
   - `/api/csv/upload`: Handles multipart CSV upload and bulk record parsing.
4. **Interactive Embedded Frontend:**
   - Serves the complete **Artistic UPI QR Studio** with multi-language Indian scripts (Hindi, Tamil, Telugu, Malayalam, Kannada, Bengali), Canva/Notion/Ghibli styling, live preview dock, and high-resolution standee printing.

---

## 🛠️ How to Run Locally

### Prerequisites:
- Java JDK 17 or higher
- Apache Maven 3.8+ (or use `./mvnw`)

### Commands:
```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/My-security-project.git
cd My-security-project

# 2. Build and run using Maven
mvn clean spring-boot:run
```

Once started, open your browser at:
👉 **`http://localhost:8080`**

---

## 📤 How to Push to GitHub

```bash
# Initialize and push to your new GitHub repository
git init
git add .
git commit -m "Initial commit: Spring Boot 3 Security & UPI QR Studio"
git branch -M main
git remote add origin https://github.com/<your-username>/My-security-project.git
git push -u origin main
```
