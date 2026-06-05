# Requirements Coverage & Remaining-Work Plan

Status of the full exam specification against the current codebase.
**✅ Done · 🟡 Partial · ⬜ Not yet**

---

## Registration Security
| Requirement | Status | Where |
|---|---|---|
| Verify account before activation | ✅ | `AuthServiceImpl.signup` (INACTIVE) + `verify-account` |
| 6-digit OTP | ✅ | `OtpServiceImpl` (`app.otp.length=6`) |
| OTP stored securely in DB | ✅ | `otp_tokens` table, single-use |
| OTP expiration configurable | ✅ | `app.otp.expiry-minutes` |
| Status INACTIVE until verified | ✅ | signup sets INACTIVE |
| Prevent duplicate OTP verification | ✅ | `used` flag + `invalidateOutstanding` |

## Authentication
| Requirement | Status | Notes |
|---|---|---|
| JWT access token | ✅ | `JwtService` |
| BCrypt hashing | ✅ | `SecurityConfig` BCrypt |
| Inactive users cannot login | ✅ | `DisabledException` → 401 |
| Unverified users cannot login | ✅ | unverified = INACTIVE |
| JWT **refresh token** | ⬜ | *planned (Phase 1)* |
| **Logout** endpoint | ⬜ | *planned (Phase 1)* |
| Token **blacklist/invalidation** | ⬜ | *planned (Phase 1)* |

## Password Security
| Requirement | Status |
|---|---|
| Min 8 chars, upper+lower+number+special | ✅ `@Pattern` on signup & reset |
| Password never returned in responses | ✅ DTOs never expose it |
| Password history validation (optional) | ⬜ optional, *Phase 3* |

## Forgot Password
| Requirement | Status |
|---|---|
| Forgot-password endpoint | ✅ |
| Generate OTP / reset token | ✅ |
| Send reset email | ✅ |
| Verify OTP before reset | ✅ |
| Secure password update | ✅ |

## Validation
| Area | Status | Notes |
|---|---|---|
| User: full name blank + length | ✅ | `@NotBlank` + `@Size(max=120)` |
| Email blank / RFC format | ✅ | `@NotBlank @Email` |
| Email lowercase only | ✅ | normalised in `AuthServiceImpl` + login |
| Country code default = +250 | ⬜ | *Phase 2* (phone normaliser) |
| Phone format | ✅ | `@Pattern` |
| Duplicate email | ✅ | checked in service |
| Customer: nationalId required+unique, email, phone, address | ✅ | `CustomerServiceImpl` / `CustomerRequest` |
| Meter: number unique, type required | ✅ | |
| Meter: installation date not future | ✅ | `@PastOrPresent` |
| Reading: prev≥0, current>prev, 1/month/yr, date, active meter | ✅ | `MeterReadingServiceImpl` |
| Billing: no duplicate, customer active, **meter active** | ✅ | `BillServiceImpl` (meter-active check added) |
| Payment: amount>0, ≤ outstanding, bill exists, status check | ✅ | `PaymentServiceImpl` |

## API Design
| Requirement | Status | Notes |
|---|---|---|
| DTO pattern (never expose entities) | ✅ | request/response records + `EntityMapper` |
| Layered architecture | ✅ | controller/service/repository/entity/dto/mapper/security/exception |
| **Standard response envelope** `{success,message,data,timestamp}` | 🟡 | errors use `ApiError`; success returns raw DTO. *Phase 1 wraps everything* |

## Exception Handling
| Requirement | Status |
|---|---|
| `@RestControllerAdvice` global handler | ✅ |
| RNF / Duplicate / Validation / Authentication / AccessDenied / BusinessRule / Generic | ✅ all handled |

## Swagger
| Requirement | Status |
|---|---|
| Document every endpoint + DTOs | ✅ |
| Auth instructions + JWT bearer config | ✅ `OpenApiConfig` |
| Example requests/responses | ✅ `@Schema(example=...)` |
| Login example uses placeholders (not real admin) | ✅ |
| Show required roles per endpoint | 🟡 in operation descriptions (not a formal field) |

## Email Service
| Requirement | Status | Notes |
|---|---|---|
| EmailService abstraction | ✅ | `EmailService` / `EmailServiceImpl` (SMTP) |
| OTP verification email | ✅ | |
| Account verification email | ✅ | |
| Password reset email | ✅ | |
| **Bill notification email** | ✅ | `NotificationServiceImpl` now emails on bill generation |
| **Payment confirmation email** | ✅ | emailed on full payment |
| **Welcome email** | ⬜ | *Phase 2* (send after verify-account) |

## Role-Based Access Matrix
| Requirement | Status | Notes |
|---|---|---|
| ADMIN / OPERATOR / FINANCE / CUSTOMER mapped | ✅ | `@PreAuthorize` per endpoint |
| Customer views **own** profile/bills/payments/notifications | 🟡 | endpoints exist but use `{customerId}` path — *ownership enforcement is Phase 2* |

---

## Larger items NOT yet implemented (need explicit build-out)
| # | Feature | Effort | Phase |
|---|---|---|---|
| 1 | Standard `{success,message,data,timestamp}` envelope across **all** controllers | Medium (touches every controller) | 1 |
| 2 | Refresh tokens + logout + token blacklist | Medium | 1 |
| 3 | Pagination + sorting + search on all list endpoints (`page,size,sortBy,sortDirection`) | Medium | 1 |
| 4 | Audit `createdBy` / `updatedBy` via `AuditorAware` | Small | 2 |
| 5 | Ownership checks (customer sees only their own data) | Small–Medium | 2 |
| 6 | File upload/download (profile pictures, documents) with type/size validation | Medium | 2 |
| 7 | Welcome email + phone `+250` normaliser | Small | 2 |
| 8 | Password history | Small (optional) | 3 |

### Suggested phase order
- **Phase 1 (highest grading impact):** response envelope, pagination/search, refresh-token + logout/blacklist.
- **Phase 2:** auditing (createdBy/updatedBy), ownership checks, file management, welcome email, +250 default.
- **Phase 3 (optional polish):** password history, financial-report endpoints for FINANCE.

> Everything marked ✅ has been verified compiling and (for the core flows) running live on H2.
> Tell me which phase to build next and I'll implement it end-to-end with the same verification.
