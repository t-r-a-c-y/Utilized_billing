# Testing Guide — Utility Billing System

Two ready-to-use test artifacts ship with the project:

| File | Tool | How to use |
|------|------|------------|
| [postman/UtilityBilling.postman_collection.json](postman/UtilityBilling.postman_collection.json) | Postman | Import → it has folders 1–9 incl. **Bad Cases** + auto-saves the JWT |
| [requests.http](requests.http) | IntelliJ HTTP Client | Open it → click the ▶ next to any request (no extra tool needed) |

---

## Getting the OTP (important)

Login is **two-step** and OTPs are emailed. Because the seeded accounts use fake
`@utility.rw` addresses, **read their OTP from the application console**. After
`POST /auth/login` you'll see in the IntelliJ Run tab:

```
OtpServiceImpl : OTP for admin@utility.rw [LOGIN] = 482915 (valid 10 min)
```

Paste `482915` into the `otp` variable (Postman) / `@otp` (requests.http), then run the
verify request. Other ways to read it: query `SELECT * FROM otp_tokens ORDER BY id DESC;`,
or use a **real email** address when signing up new users so the code arrives in the inbox.

---

## Happy-path order

1. `POST /auth/login` (admin) → read OTP → `POST /auth/verify-otp` → JWT saved.
2. `POST /customers` → create customer (id 1).
3. `POST /meters` → create meter (id 1) for customer 1.
4. `POST /config/tariffs`, `/config/taxes`, `/config/penalties` → pricing config.
5. `POST /readings` → capture reading (OPERATOR or ADMIN token).
6. `POST /bills/generate` → bill created as **PENDING** + a notification is stored.
7. `PATCH /bills/{id}/approve` → **APPROVED**.
8. `POST /payments` → partial = **PARTIALLY_PAID**, full = **PAID** (+ "paid" notification).
9. `GET /notifications/customer/1` → see the stored messages.

**Worked billing example** (electricity, 120 units, tiered 0–20@89 / 21–50@212 / 51+@249,
service 1500, VAT 18%):
```
tariff = 20*89 + 30*212 + 70*249 = 1780 + 6360 + 17430 = 25570
+ service 1500 = 27070 ; VAT 18% = 4872.60 ; total = 31942.60 FRW
```

---

## Bad-case scenarios & expected results

| # | Scenario | Request | Expected |
|---|----------|---------|----------|
| 1 | No token on secured endpoint | `GET /users` (no header) | **401** Unauthorized |
| 2 | Wrong password | `POST /auth/login` bad pass | **401** "Invalid email or password" |
| 3 | Inactive/unverified account login | login before `verify-account` | **401** "Account is inactive…" |
| 4 | Wrong / expired / reused OTP | `POST /auth/verify-otp` bad code | **422** "Invalid OTP" / "expired" / "No active OTP" |
| 5 | Wrong role | OPERATOR calls `POST /customers` | **403** Forbidden |
| 6 | Duplicate customer (National ID) | repeat `POST /customers` | **409** Conflict |
| 7 | Duplicate customer (email) | new ID, existing email | **409** Conflict |
| 8 | Validation errors | empty/invalid fields | **400** with `fieldErrors` map |
| 9 | Duplicate meter number | repeat `POST /meters` | **409** Conflict |
| 10 | Reading on INACTIVE meter | capture on inactive meter | **422** "…INACTIVE and cannot receive readings" |
| 11 | current ≤ previous reading | `currentReading < previous` | **422** "must be greater than previous" |
| 12 | Duplicate reading (meter+month+year) | repeat same period | **409** Conflict |
| 13 | Bill for INACTIVE customer | generate for inactive customer | **422** "…INACTIVE and cannot be billed" |
| 14 | Duplicate bill (meter+month+year) | repeat `POST /bills/generate` | **409** Conflict |
| 15 | Pay a PENDING (unapproved) bill | pay before approve | **422** "not yet approved" |
| 16 | Overpay (amount > outstanding) | huge `amountPaid` | **422** "exceeds the outstanding balance" |
| 17 | Pay an already PAID bill | pay a settled bill | **422** "already fully paid" |
| 18 | Not found | `GET /customers/9999` | **404** Not Found |

All errors come back in the standard envelope:
```json
{ "timestamp":"...", "status":409, "error":"Conflict",
  "message":"Customer already exists with National ID: 1199080012345678",
  "path":"/api/v1/customers", "fieldErrors": null }
```

---

## How to reproduce the role test (403)

1. `POST /auth/login` with `operator@utility.rw` / `Operator123!`, read OTP, `verify-otp` → operator JWT.
2. With that token call `POST /api/v1/customers` → **403 Forbidden** (creating customers is ADMIN-only).
3. But `POST /api/v1/readings` with the same token → **201** (operators may capture readings).
