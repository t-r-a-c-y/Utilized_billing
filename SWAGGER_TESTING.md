# Testing with Swagger UI — Utility Billing System

Open: **http://localhost:8080/swagger-ui.html**
(Start the app first; if you changed config, restart it and hard-refresh with `Ctrl+F5`.)

The page shows groups **1. Authentication … 9. Notifications**. Each endpoint:
click it → **Try it out** → edit the JSON → **Execute** → read the response code/body below.

---

## STEP 0 — Authorize (two-step OTP login)

You must get a JWT and paste it into Swagger's **Authorize** box before the secured
endpoints work.

1. **1. Authentication → `POST /auth/login` → Try it out**, body:
   ```json
   { "email": "admin@utility.rw", "password": "Admin123!" }
   ```
   **Execute** → response says an OTP was sent.
2. **Read the OTP from the IntelliJ Run console:**
   ```
   OtpServiceImpl : OTP for admin@utility.rw [LOGIN] = 482915 (valid 10 min)
   ```
3. **`POST /auth/verify-otp` → Try it out**, body (use your code):
   ```json
   { "email": "admin@utility.rw", "otp": "482915" }
   ```
   **Execute** → copy the `token` value from the response.
4. Click the green **Authorize** button (top-right) → paste the token → **Authorize** → **Close**.
   (Do **not** type "Bearer " — Swagger adds it.) Now every call is authenticated.

> The token is for ROLE_ADMIN. To test operator/finance-only rules, repeat steps 1–4 with
> `operator@utility.rw / Operator123!` or `finance@utility.rw / Finance123!` and re-Authorize.

---

## Happy path (run in this order)

### 3. Customers → `POST /customers`
```json
{ "fullNames": "John Habimana", "nationalId": "1199080012345678",
  "email": "john@example.rw", "phoneNumber": "+250788654321",
  "address": "KG 11 Ave, Kigali", "status": "ACTIVE" }
```
✅ **201** → note the returned `id` (should be 1).

### 4. Meters → `POST /meters`
```json
{ "meterNumber": "MTR-EL-0001", "meterType": "ELECTRICITY",
  "installationDate": "2025-01-15", "customerId": 1, "status": "ACTIVE" }
```
✅ **201**.

### 6. Tariffs, Taxes & Penalties → `POST /config/tariffs`
```json
{ "name": "Electricity Residential 2026", "meterType": "ELECTRICITY",
  "tariffType": "TIERED", "serviceCharge": 1500.00, "effectiveStart": "2026-01-01",
  "tiers": [ { "upToUnit": 20, "ratePerUnit": 89 },
             { "upToUnit": 50, "ratePerUnit": 212 },
             { "upToUnit": null, "ratePerUnit": 249 } ] }
```
Then `POST /config/taxes`:
```json
{ "name": "VAT", "percentage": 18.00, "effectiveStart": "2026-01-01" }
```
Then `POST /config/penalties`:
```json
{ "name": "Late payment penalty", "percentage": 5.00, "effectiveStart": "2026-01-01" }
```
✅ **201** each.

### 5. Meter Readings → `POST /readings`
```json
{ "meterId": 1, "currentReading": 1320.00, "readingDate": "2026-05-31",
  "month": 5, "year": 2026 }
```
✅ **201**, `consumption = 1320`. *(ADMIN token works; OPERATOR also allowed.)*

### 7. Bills → `POST /bills/generate`
```json
{ "meterId": 1, "month": 5, "year": 2026, "dueInDays": 15 }
```
✅ **201** → status `PENDING`. Note the `id` and `billReference` (e.g. `BILL-2026-05-000001`).
A notification is created automatically.

### 7. Bills → `PATCH /bills/{id}/approve`
Put the bill `id` (e.g. `1`) in the path field → **Execute**.
✅ **200** → status `APPROVED`.

### 8. Payments → `POST /payments`  (partial then full)
```json
{ "billReference": "BILL-2026-05-000001", "amountPaid": 5000.00,
  "paymentMethod": "MOBILE_MONEY", "paymentDate": "2026-06-05" }
```
✅ **201** → bill becomes `PARTIALLY_PAID`. Pay the remaining `outstandingBalance` again →
bill becomes `PAID` and a "fully paid" notification is created.

### 9. Notifications → `GET /notifications/customer/1`
✅ **200** → see the "bill processed" and "fully paid" messages.

---

## Bad-case tests in Swagger (what to expect)

| Try this in Swagger | Expected |
|---------------------|----------|
| Any secured endpoint **before** Authorize (or click **Authorize → Logout** first) | **401** |
| `POST /auth/login` with password `WRONG` | **401** Invalid email or password |
| Login a freshly signed-up (unverified) account | **401** "Account is inactive…" |
| `POST /auth/verify-otp` with `otp: "000000"` | **422** Invalid/expired/no active OTP |
| Re-`POST /customers` with the same `nationalId` | **409** Conflict |
| `POST /customers` with empty `fullNames`, bad `email` | **400** with `fieldErrors` |
| Re-`POST /meters` with the same `meterNumber` | **409** Conflict |
| `POST /readings` with `currentReading` ≤ previous (e.g. 1000) | **422** must be greater |
| Re-`POST /readings` for meter 1, month 5, year 2026 | **409** Conflict |
| `POST /payments` on a bill that's still `PENDING` (not approved) | **422** not yet approved |
| `POST /payments` with `amountPaid: 99999999` | **422** exceeds outstanding balance |
| Authorize as **operator**, then `POST /customers` | **403** Forbidden (ADMIN-only) |
| `GET /customers/9999` | **404** Not Found |

To test the **403 role** case: Authorize with `operator@utility.rw` (login → console OTP →
verify-otp → re-Authorize), then `POST /customers` → **403**, but `POST /readings` → **201**.

---

## Quick tips
- The **Schemas** section at the bottom of Swagger lists every request/response model with
  the example values you see in the "Try it out" bodies.
- If a secured call returns **401** unexpectedly, your token expired (24h) or you forgot to
  re-Authorize after switching roles — just redo Step 0.
- Reading OTPs: always from the **Run console** (`OTP for … = 123456`) for the seeded accounts.
