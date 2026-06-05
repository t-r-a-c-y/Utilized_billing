# WASAC Integrated-Situation Compliance Map

How the implementation satisfies the WASAC / REG utility-billing brief. **All tasks ✅.**

## Task 1 — User Management & Security (JWT)
| Requirement | Status | Where |
|---|---|---|
| Full names | ✅ | `User.fullNames` |
| Email (username) | ✅ | `User.email` (unique, lowercase) |
| Phone: **country code (default +250) + number** | ✅ | `User.countryCode` (default `+250`) + `User.phoneNumber`; `SignupRequest` two fields |
| Password ≥ 8 chars + symbol + validation | ✅ | `@Pattern` on `SignupRequest`/`ResetPasswordRequest` (upper+lower+digit+special) |
| Status (Active/Inactive) | ✅ | `User.status`; inactive can't log in |
| ROLE_ADMIN / OPERATOR / FINANCE / CUSTOMER | ✅ | `Role` enum + `@PreAuthorize` |
| Signup + login | ✅ | `/auth/signup`, two-step `/auth/login` + `/auth/verify-otp` |
| All endpoints secured except auth | ✅ | `SecurityConfig` (permitAll only `/auth`,`/swagger`,`/h2`) |
| ADMIN configures tariffs / approves bills / manages users | ✅ | tariff, bill-approve, user endpoints |
| **OPERATOR captures readings (only)** | ✅ | `POST /readings` = `hasRole('OPERATOR')` — admin gets 403 |
| FINANCE approves bills + payments | ✅ | bill-approve + payments = FINANCE/ADMIN |
| **CUSTOMER views bills + payment history** | ✅ | `/bills/my`, `/payments/my`, `/notifications/my`, `/customers/me` (own data via JWT) |

## Task 2 — Customer & Meter Management
| Requirement | Status |
|---|---|
| Customer: full names, National ID (unique), email, phone, address, status | ✅ |
| Prevent duplicate customer registration | ✅ (National ID + email unique, 409) |
| Inactive customers can't receive bills | ✅ (`BillServiceImpl` guard) |
| Meter: number (unique), type (water/electricity), installation date (verified, not future), status | ✅ (`@PastOrPresent`, unique number) |
| Customer may have one or more meters | ✅ (claim model; a meter has one owner) |

## Task 3 — Meter Reading Management
| Requirement | Status |
|---|---|
| Operator captures: meter, previous, current, reading date | ✅ |
| Current > previous | ✅ (422) |
| One reading per meter per month/year | ✅ (409) |
| Meter must be active | ✅ (422) |
| (Extra) reading date must fall in the stated month/year | ✅ (422) |

## Task 4 — Tariff, Tax & Penalty Configuration
| Requirement | Status |
|---|---|
| Consumption tariffs flat or tiered | ✅ (`TariffType` FLAT/TIERED + tiers) |
| Fixed service charges | ✅ (`Tariff.serviceCharge`) |
| VAT / other taxes | ✅ (`Tax`, percentage, versioned) |
| Late-payment penalties | ✅ (`Penalty`, versioned) |
| Tariffs versioned | ✅ (`version` + effective dates) |
| New tariffs apply only to future cycles | ✅ (effective window resolved by billing-cycle date) |

## Task 5 — Payment Processing
| Requirement | Status |
|---|---|
| Record: bill reference, amount, method, date | ✅ (`PaymentRequest`) |
| Partial + full payment | ✅ |
| Auto-update outstanding balance | ✅ |
| Mark PAID when balance = 0 (else PARTIALLY_PAID) | ✅ |
| Cannot pay more than outstanding | ✅ (422) |

## Task 6 — Database Routines & Messaging
| Requirement | Status |
|---|---|
| Trigger / stored procedure / cursor | ✅ (`db/postgres_routines.sql` + `db/mysql_routines.sql`: 2 triggers, 1 stored proc, 1 cursor) |
| On bill generation → insert notification | ✅ (trigger + `NotificationService`) |
| On full payment → update status + notify customer | ✅ |
| Exact message format | ✅ "Dear &lt;CustomerName&gt;, Your &lt;Month/Year&gt; utility bill of &lt;Amount&gt; FRW has been successfully processed." |
| (Extra) notification also emailed via SMTP | ✅ |

## Instructions
| Requirement | Status |
|---|---|
| ERD designed first | ✅ [ERD.md](ERD.md) |
| Spring Boot + Spring Data JPA, generated APIs | ✅ |
| Spring Boot flow diagram | ✅ [ARCHITECTURE.md](ARCHITECTURE.md) |
| Records added via Postman/Swagger/main-class/DBMS | ✅ (Swagger + seeded data + sample SQL) |
| Swagger UI API docs | ✅ `/swagger-ui.html` |
| JWT auth/authorization | ✅ |
| All task rules enforced | ✅ (see [REQUIREMENTS_COVERAGE.md](REQUIREMENTS_COVERAGE.md) for the full validation list) |

> **Beyond the brief** (already built): email OTP for signup/login/reset, soft-delete
> (deactivate, never hard-delete customers — preserves audit), per-bill VAT, overdue-penalty
> endpoint, standard `ApiError` envelope, global exception handler.
