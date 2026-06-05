# Utility Billing System Backend

A robust, enterprise-grade backend built with **Spring Boot 3.3** and **Java 21** designed for a national utility company. This system orchestrates the modern migration of electricity services from legacy prepaid models to a unified postpaid model alongside existing water services.

---

## 🚀 Key Features

* **Dual-Service Postpaid Billing:** Unified billing engine for both Water and Electricity.
* **Granular Meter Management:** Tracks physical meter allocations, historical types, and assignments.
* **Versioned Configurations:** Flexible engines for Tiered Tariffs, VAT/Taxes, and Late Penalties by effective dates.
* **Automated Calculations:** Custom SQL triggers, routines, and cursors calculate bills precisely.
* **Two-Step Authentication:** Secure JWT-based access paired with mandatory multi-step Email OTP verification.
* **Asynchronous Notifications:** Event-driven customer alert queuing for generated bills and confirmation of payments.

---

## 🛠️ Technology Stack

* **Core Platform:** Java 21 / Spring Boot 3.3
* **Security Architecture:** Spring Security 6 / JSON Web Tokens (jjwt) / BCrypt
* **Data Access Layer:** Spring Data JPA / Hibernate / Validation (JSR-380)
* **API Documentation:** springdoc-openapi (Swagger UI)
* **Database Support:** H2 (In-Memory Development) / MySQL 8 / PostgreSQL
* **Tooling & Build System:** Maven / Project Lombok

> 💡 **Detailed System Design:** For complete Entity-Relationship Diagrams (ERD), stateful security workflows, and the complete role-permission matrix, please refer directly to [DESIGN.md](DESIGN.md).

---

## ⚙️ Configuration & Execution

### Option A: Zero Setup (In-Memory H2 - Default)
Ideal for instant demos, local verification, or prototyping.
```bash
mvn spring-boot:run
```
* **API Base URL:** `http://localhost:8080`
* **Swagger UI Documentation:** `http://localhost:8080/swagger-ui.html`
* **H2 Management Console:** `http://localhost:8080/h2-console`
  * *JDBC URL:* `jdbc:h2:mem:utilitydb`
  * *User Name:* `sa` | *Password:* (Leave Blank)

*Note: In IntelliJ IDEA, you can simply right-click and run the `UtilityBillingApplication` main class directly.*

### Option B: Production-Ready MySQL Execution
1. Boot the application engine with the MySQL profile active:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```
   *(Optionally override credentials by injecting `DB_USER` and `DB_PASSWORD` system environment variables)*
2. Inject the transactional procedures, cursors, and triggers into your database instance:
   ```bash
   mysql -u root -p utility_billing < src/main/resources/db/mysql_routines.sql
   ```

### Option C: PostgreSQL Execution
Ensure your PostgreSQL instance is running locally, then initialize with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## 🔑 Default Seeded Accounts
The system automatically scaffolds three administrative accounts upon first initialization. These system profiles bypass registration verification but require mandatory two-step OTP checks during execution.


| Role | System Email | Master Password |
| :--- | :--- | :--- |
| **ADMIN** | `admin@utility.rw` | `Admin123!` |
| **OPERATOR** | `operator@utility.rw` | `Operator123!` |
| **FINANCE** | `finance@utility.rw` | `Finance123!` |

---

## 🔒 Authentication & OTP Protocol

All registration and account resets are reinforced by transactional One-Time Passwords (OTP). By default, if real SMTP settings are omitted, the engine prints active security tokens directly into the console logs (`app.otp.log-to-console=true`).

### Optional: Real SMTP Configuration
To route structural emails over a live mail server, establish these environment variables prior to system launch:
```bash
# Windows PowerShell Environment Setup
$env:MAIL_USERNAME="your-address@gmail.com"
$env:MAIL_PASSWORD="your-secure-apps-password"
```

### Authentication Flow API Breakdown


| Step | HTTP Method | Endpoint | Request Payload Requirements | Expected System Outcome |
| :--- | :--- | :--- | :--- | :--- |
| **1** | `POST` | `/auth/signup` | `name`, `email`, `phone`, `password`, `role` | `INACTIVE` customer created; signup OTP generated and sent. |
| **2** | `POST` | `/auth/verify-account` | `email`, `otp` | Profile transitions into `ACTIVE` state. |
| **3** | `POST` | `/auth/login` | `email`, `password` | Initial validation; secondary access OTP dispatched. |
| **4** | `POST` | `/auth/verify-otp` | `email`, `otp` | Secure Bearer JWT token emitted to client. |
| **5** | `POST` | `/auth/forgot-password`| `email` | Recovery token issued. |
| **6** | `POST` | `/auth/reset-password` | `email`, `otp`, `newPassword` | Safe updates applied across security records. |

---

## 🕹️ End-to-End Testing Flow

Execute this canonical scenario inside Postman or Swagger UI to test the core billing workflows:

### 1. Security Authorization
* Invoke `POST /api/v1/auth/login` with your default **ADMIN** credentials.
* Extract the verification code from your terminal log console, and execute `POST /api/v1/auth/verify-otp`.
* Copy the returned JWT token, select **Authorize** at the top of Swagger UI, and paste it into the value field.

### 2. Base Resource Allocations
* **Register a New Customer:**
  ```http
  POST /api/v1/customers
  {
    "fullNames": "John Habimana",
    "nationalId": "1199080012345678",
    "email": "john@example.rw",
    "phoneNumber": "+250788654321",
    "address": "KG 11 Ave, Kigali",
    "status": "ACTIVE"
  }
  ```
* **Provision a System Meter:**
  ```http
  POST /api/v1/meters
  {
    "meterNumber": "MTR-EL-0001",
    "meterType": "ELECTRICITY",
    "installationDate": "2025-01-15",
    "customerId": 1,
    "status": "ACTIVE"
  }
  ```

### 3. Financial Matrix Configurations (Admin Access)
* **Define Tiered Tariff Structures:**
  ```http
  POST /api/v1/config/tariffs
  {
    "name": "Electricity Residential 2026",
    "meterType": "ELECTRICITY",
    "tariffType": "TIERED",
    "serviceCharge": 1500.00,
    "effectiveStart": "2026-01-01",
    "tiers": [
      {"upToUnit": 20, "ratePerUnit": 89},
      {"upToUnit": 50, "ratePerUnit": 212},
      {"upToUnit": null, "ratePerUnit": 249}
    ]
  }
  ```
* **Inject Tax and Penalty Engines:**
  * `POST /api/v1/config/taxes` \(\rightarrow\) `{ "name": "VAT", "percentage": 18.00, "effectiveStart": "2026-01-01" }`
  * `POST /api/v1/config/penalties` \(\rightarrow\) `{ "name": "Late payment penalty", "percentage": 5.00, "effectiveStart": "2026-01-01" }`

### 4. Billing Operations & Balances
* **Capture Meter Performance (Log in as OPERATOR first):**
  ```http
  POST /api/v1/readings
  {
    "meterId": 1,
    "currentReading": 1320.00,
    "readingDate": "2026-05-31",
    "month": 5,
    "year": 2026
  }
  ```
* **Generate Monthly Invoice:**
  ```http
  POST /api/v1/bills/generate
  {
    "meterId": 1,
    "month": 5,
    "year": 2026,
    "dueInDays": 15
  }
  ```
  *(Creates a `PENDING` bill status and routes a delivery notification to the pipeline)*
* **Approve Invoices (ADMIN/FINANCE Roles Only):**
  * Execute `PATCH /api/v1/bills/{id}/approve`
* **Process Balances:**
  ```http
  POST /api/v1/payments
  {
    "billReference": "BILL-2026-05-000001",
    "amountPaid": 5000.00,
    "paymentMethod": "MOBILE_MONEY",
    "paymentDate": "2026-06-05"
  }
  ```
  *(Partial transaction payments set the state to `PARTIALLY_PAID`. Completing the true total changes the invoice state to `PAID` and issues a payment confirmation alert)*

* **Audit Historic Notification Log:**
  * Execute `GET /api/v1/notifications/customer/1`

---

## 📂 Repository Blueprint

```text
├── src
│   ├── main
│   │   ├── java/com/utility/billing      # Application Source Code
│   │   └── resources
│   │       ├── application.properties    # Base configurations
│   │       └── db                        # Relational definitions & SQL routines
│   └── test                              # Integration & Unit Assertions
├── DESIGN.md                             # Architectural, ERD, & Flow charts
└── pom.xml                               # Maven dependency specifications
```
