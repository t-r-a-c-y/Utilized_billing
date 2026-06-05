# System Architecture — Utility Billing System

A layered Spring Boot 3 / Java 21 backend with stateless JWT security, OTP-based
auth (email delivery), versioned billing configuration, and DB-side routines.
Diagrams below are in Mermaid (render with the IntelliJ **Mermaid** plugin or at
<https://mermaid.live>) plus a plain-text fallback.

---

## A. High-level component architecture

```mermaid
flowchart TB
    subgraph Clients
        UI["Swagger UI / Postman / Frontend"]
    end

    subgraph Security["Security layer (stateless)"]
        FILTER["JwtAuthenticationFilter\nvalidate Bearer token"]
        ENTRY["JwtAuthenticationEntryPoint\n401 JSON"]
        SC["SecurityConfig\npermitAll: /auth,/swagger\nauthenticated: rest\n@PreAuthorize roles"]
    end

    subgraph Web["Controller layer (@RestController)"]
        AC["AuthController"]
        UC["UserController"]
        CC["CustomerController"]
        MC["MeterController"]
        RC["MeterReadingController"]
        TC["TariffController"]
        BC["BillController"]
        PC["PaymentController"]
        NC["NotificationController"]
    end

    subgraph Service["Service layer (@Service, @Transactional, business rules)"]
        AS["AuthService"]
        OS["OtpService"]
        ES["EmailService"]
        US["UserService"]
        CS["CustomerService"]
        MS["MeterService"]
        RS["MeterReadingService"]
        TS["TariffService"]
        BS["BillService"]
        PS["PaymentService"]
        NS["NotificationService"]
        JWT["JwtService"]
    end

    subgraph Data["Persistence layer (Spring Data JPA)"]
        REPO["Repositories"]
        MAP["EntityMapper (entity ↔ DTO)"]
    end

    DB[("Relational DB\nPostgreSQL / MySQL / H2\n+ triggers, stored proc, cursor")]
    SMTP[["SMTP (Gmail)\nOTP & notification emails"]]
    EXC["GlobalExceptionHandler\n@RestControllerAdvice → ApiError"]

    UI --> FILTER --> SC --> Web
    SC -. unauthenticated .-> ENTRY
    AC --> AS
    AS --> OS --> ES --> SMTP
    AS --> JWT
    UC --> US
    CC --> CS
    MC --> MS
    RC --> RS
    TC --> TS
    BC --> BS
    PC --> PS
    NC --> NS
    BS --> NS --> ES
    PS --> NS
    Service --> REPO --> DB
    Service --> MAP
    Web -. any exception .-> EXC
```

---

## B. Request lifecycle (one secured call)

```mermaid
sequenceDiagram
    participant C as Client
    participant F as JwtAuthenticationFilter
    participant S as SecurityFilterChain
    participant Ctl as Controller
    participant Svc as Service (@Transactional)
    participant Repo as Repository
    participant DB as Database
    C->>F: HTTP + Authorization: Bearer JWT
    F->>F: parse & validate token, load UserDetails
    F->>S: SecurityContext populated
    S->>S: @PreAuthorize role check
    S->>Ctl: invoke handler (@Valid DTO)
    Ctl->>Svc: call service method
    Svc->>Repo: query/persist (session open)
    Repo->>DB: SQL
    DB-->>Repo: rows
    Repo-->>Svc: entities
    Svc->>Svc: EntityMapper → DTO (inside tx)
    Svc-->>Ctl: response DTO
    Ctl-->>C: JSON (or ApiError via GlobalExceptionHandler)
```

---

## C. Two-step OTP authentication flow

```mermaid
flowchart LR
    A[POST /auth/signup\nrole=CUSTOMER +nationalId+address] --> B[(User INACTIVE\n+ Customer profile)]
    B --> C[OTP emailed\nSIGNUP_VERIFICATION]
    C --> D[POST /auth/verify-account] --> E[(User ACTIVE)]
    E --> F[POST /auth/login\nemail+password] --> G[OTP emailed\nLOGIN]
    G --> H[POST /auth/verify-otp] --> I[JWT issued]
    I --> J[Authorized API calls]
    K[POST /auth/forgot-password] --> L[OTP emailed\nPASSWORD_RESET]
    L --> M[POST /auth/reset-password] --> N[(password updated)]
```

---

## D. Plain-text layered view (fallback)

```
            ┌───────────────────────────────────────────────────────┐
 Clients →  │  Swagger UI · Postman · Frontend (HTTP + Bearer JWT)   │
            └───────────────────────────┬───────────────────────────┘
                                        │
        ┌───────────────────────────────▼─────────────────────────────┐
 SEC    │ JwtAuthenticationFilter → SecurityFilterChain (stateless)    │
        │ permitAll(/auth,/swagger,/h2) · authenticated(rest)          │
        │ @EnableMethodSecurity + @PreAuthorize(role)                  │
        │ unauthenticated → JwtAuthenticationEntryPoint (401 JSON)     │
        └───────────────────────────────┬─────────────────────────────┘
                                        │
 WEB    │ Auth · User · Customer · Meter · Reading · Tariff · Bill ·   │
        │ Payment · Notification controllers  (@Valid request DTOs)    │
        └───────────────────────────────┬─────────────────────────────┘
                                        │   (any exception)
 SVC    │ *Service + *ServiceImpl  (@Transactional, business rules)    │──► GlobalExceptionHandler
        │ Auth/Otp/Email/Jwt · Customer · Meter · Reading · Tariff ·   │     (@RestControllerAdvice
        │ Bill · Payment · Notification        EntityMapper(entity→DTO)│      → ApiError JSON)
        └───────┬───────────────────────────────────┬─────────────────┘
                │                                   │
 DATA   │ Spring Data JPA Repositories │     │ EmailService → SMTP (Gmail)  │
        └───────┬──────────────────────┘     └──────────────────────────────┘
                │
        ┌───────▼───────────────────────────────────────────────────┐
 DB     │ PostgreSQL / MySQL / H2                                     │
        │ tables + TRIGGER (notify on bill insert / paid)            │
        │        + STORED PROCEDURE + CURSOR (overdue penalties)     │
        └────────────────────────────────────────────────────────────┘
```

---

## E. Package map

```
com.utility.billing
├── config        SecurityConfig, OpenApiConfig, DataInitializer
├── security      JwtService, JwtAuthenticationFilter, JwtAuthenticationEntryPoint,
│                 CustomUserDetailsService
├── controller    9 @RestControllers
├── service       interfaces  +  impl/  (business logic, @Transactional)
├── repository    Spring Data JPA interfaces
├── entity        JPA entities  +  enums/
├── dto           request/  response/   (Java records, Bean Validation)
├── mapper        EntityMapper
└── exception     GlobalExceptionHandler, ApiError, custom exceptions
```

**Cross-cutting concerns**
- **Security:** stateless JWT (HS256), BCrypt, role-based `@PreAuthorize`, OTP 2FA.
- **Validation:** Bean Validation on every DTO + business-rule checks in services.
- **Auditing:** `BaseEntity` with `@CreatedDate`/`@LastModifiedDate` (JPA Auditing).
- **Email:** `EmailService` SMTP abstraction for OTP + bill/payment notifications.
- **Error handling:** one `@RestControllerAdvice` → consistent `ApiError` envelope.
- **DB routines:** trigger + stored procedure + cursor (see `src/main/resources/db`).
```
