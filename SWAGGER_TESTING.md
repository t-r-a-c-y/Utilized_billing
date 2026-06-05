# Full Swagger Test Plan — Utility Billing System

Open **http://localhost:8080/swagger-ui.html** (start the app first; after any config
change, restart and hard-refresh with `Ctrl+F5`).

This plan exercises **every endpoint and every HTTP method** (POST, GET, PUT, PATCH, DELETE)
and proves **validation + no-duplicate** rules. It uses **4 customers**:

| # | Name | Email | Role in test |
|---|------|-------|--------------|
| 1 | **Tracy Tesi** | **tracytesi69@gmail.com** | **MAIN** — self-registers, claims a meter, gets billed → **real emails land here** |
| 2 | John Habimana | john@example.rw | admin-created; used for PUT (update) |
| 3 | Aline Uwase | aline@example.rw | admin-created; kept |
| 4 | Eric Niyonzima | eric@example.rw | admin-created; **deactivated** (status PATCH test — customers are never deleted) |

> 📧 **Email check:** because Tracy uses a real Gmail address and SMTP is configured,
> she receives: a **signup OTP**, a **login OTP**, a **bill notification**, and a
> **payment confirmation**. Watch that inbox (and the console — every OTP is logged too).

---

## Task coverage (WASAC Tasks 1–6) — where each is tested
| Task | What | Tested in |
|------|------|-----------|
| **1** | User mgmt + JWT (signup, two-step OTP login, roles, +250 phone, password policy) | PART 0, PART 1, PART 9, PART 10 |
| **2** | Customer & Meter mgmt (CRUD, unique National ID/meter, inactive can't be billed) | PART 2, PART 3, PART 10 |
| **3** | Meter readings (OPERATOR only, current>prev, 1/period, active meter, date match) | PART 5, PART 10 |
| **4** | Tariff (flat/tiered) + service charge + tax + penalty, versioned | PART 4 |
| **5** | Payments (partial/full, outstanding update, mark PAID) | PART 7, PART 10 |
| **6** | DB routines + messaging (trigger + stored proc + cursor) | **PART 11 (psql, below)** |

## Reading OTPs
After any OTP step, read the code from the **IntelliJ Run console**:
`OtpServiceImpl : OTP for <email> [PURPOSE] = 123456`. Tracy's also arrive by email.

---

# PART 0 — Authorize as ADMIN (needed for most calls)

**1. Auth → `POST /auth/login`** → Try it out:
```json
{ "email": "admin@utility.rw", "password": "Admin123!" }
```
Read the `[LOGIN]` OTP from the console, then **`POST /auth/verify-otp`**:
```json
{ "email": "admin@utility.rw", "otp": "PASTE_OTP" }
```
Copy `token` → click **Authorize** (top-right) → paste → **Authorize** → **Close**.

---

# PART 1 — Customer 1 (Tracy) self-registers  *(tests the customer auth + email path)*

### `POST /auth/signup`  → **201**
```json
{ "fullNames": "Tracy Tesi", "email": "tracytesi69@gmail.com",
  "countryCode": "+250", "phoneNumber": "788111001", "password": "Tracy123!",
  "role": "ROLE_CUSTOMER", "nationalId": "1199900000000001",
  "address": "KN 1 Ave, Kigali" }
```
> 📞 **Two-section phone:** `countryCode` defaults to **+250** (Rwanda) if you omit it;
> `phoneNumber` is the local number only (digits, no country code).
→ a signup OTP is **emailed to tracytesi69@gmail.com** (and logged). This becomes **Customer id 1**.

### `POST /auth/verify-account`  → **200**
```json
{ "email": "tracytesi69@gmail.com", "otp": "PASTE_SIGNUP_OTP" }
```

### Get Tracy a token: `POST /auth/login` then `POST /auth/verify-otp`
```json
{ "email": "tracytesi69@gmail.com", "password": "Tracy123!" }
```
then
```json
{ "email": "tracytesi69@gmail.com", "otp": "PASTE_LOGIN_OTP" }
```
**Save Tracy's token** somewhere — you'll re-Authorize with it for the *claim* and
*customer-view* steps. (Re-Authorize as ADMIN afterwards for admin steps.)

---

# PART 2 — Customers CRUD (ADMIN)  *(POST / GET / PUT / DELETE)*

### `POST /customers` ×3  → **201** each (creates customers 2, 3, 4)
```json
{ "fullNames": "John Habimana", "nationalId": "1199900000000002",
  "email": "john@example.rw", "phoneNumber": "+250788654322",
  "address": "KG 11 Ave, Kigali", "status": "ACTIVE" }
```
```json
{ "fullNames": "Aline Uwase", "nationalId": "1199900000000003",
  "email": "aline@example.rw", "phoneNumber": "+250788654323",
  "address": "KN 5 Rd, Kigali", "status": "ACTIVE" }
```
```json
{ "fullNames": "Eric Niyonzima", "nationalId": "1199900000000004",
  "email": "eric@example.rw", "phoneNumber": "+250788654324",
  "address": "Musanze, North", "status": "ACTIVE" }
```

### `GET /customers`  → **200** (lists all 4)
### `GET /customers/1`  → **200** (Tracy)
### `PUT /customers/2`  → **200** (update John's address/phone)
```json
{ "fullNames": "John Habimana", "nationalId": "1199900000000002",
  "email": "john@example.rw", "phoneNumber": "+250788999999",
  "address": "KG 99 Ave, Kigali", "status": "ACTIVE" }
```
### `PATCH /customers/4/status?status=INACTIVE`  → **200** (deactivate Eric)
> Customers are **never hard-deleted** — the row is kept so the audit history (bills,
> payments, notifications) survives. Deactivation just flips the status; an INACTIVE
> customer cannot receive new bills. Reactivate with `?status=ACTIVE`.
### `GET /customers` again  → **200**, still 4 customers (Eric now `INACTIVE`).

---

# PART 3 — Meters (ADMIN create + Tracy claims)  *(POST / PUT / GET / claim)*

### `POST /meters` — UNASSIGNED meter  → **201** (`customerId` omitted) → meter id 1
```json
{ "meterNumber": "MTR-EL-0001", "meterType": "ELECTRICITY",
  "installationDate": "2025-01-15" }
```
### `POST /meters` — assigned directly to John  → **201** → meter id 2
```json
{ "meterNumber": "MTR-WT-0002", "meterType": "WATER",
  "installationDate": "2025-03-05", "customerId": 2, "status": "ACTIVE" }
```
### `PUT /meters/2`  → **200** (update meter 2, e.g. status)
```json
{ "meterNumber": "MTR-WT-0002", "meterType": "WATER",
  "installationDate": "2025-03-05", "customerId": 2, "status": "ACTIVE" }
```
### `GET /meters`  → **200** ; `GET /meters/1`  → **200** (still unassigned: `customerId: null`)

### **Claim** — switch to **Tracy's token** first (Authorize → Logout → paste Tracy's token):
`POST /meters/claim/MTR-EL-0001`  → **200** → meter 1 now `customerId: 1` (Tracy).
### `GET /meters/customer/1` *(re-Authorize as ADMIN)*  → **200** → Tracy's meter listed.

---

# PART 4 — Tariff / Tax / Penalty config (ADMIN)  *(POST / GET)*

### `POST /config/tariffs`  → **201**
```json
{ "name": "Electricity Residential 2026", "meterType": "ELECTRICITY",
  "tariffType": "TIERED", "serviceCharge": 1500.00, "effectiveStart": "2026-01-01",
  "tiers": [ {"upToUnit": 20, "ratePerUnit": 89},
             {"upToUnit": 50, "ratePerUnit": 212},
             {"upToUnit": null, "ratePerUnit": 249} ] }
```
### `POST /config/taxes`  → **201** : `{ "name": "VAT", "percentage": 18.00, "effectiveStart": "2026-01-01" }`
### `POST /config/penalties`  → **201** : `{ "name": "Late payment penalty", "percentage": 5.00, "effectiveStart": "2026-01-01" }`
### `GET /config/tariffs`, `GET /config/tariffs/1`, `GET /config/taxes`, `GET /config/penalties`  → **200** each.

---

# PART 5 — Readings (OPERATOR only)  *(POST / GET)*

> ⚠️ **Only ROLE_OPERATOR may capture readings.** Re-**Authorize** with
> `operator@utility.rw / Operator123!` (login → console OTP → verify-otp) before this part.
> An ADMIN token on `POST /readings` returns **403**.

### `POST /readings`  → **201** (consumption auto = 1320)
```json
{ "meterId": 1, "currentReading": 1320.00, "readingDate": "2026-05-31",
  "month": 5, "year": 2026 }
```
### `GET /readings`  → **200** ; `GET /readings/1`  → **200** ; `GET /readings/meter/1`  → **200**
*(re-Authorize as ADMIN for the remaining parts)*

---

# PART 6 — Bills (ADMIN/FINANCE)  *(POST / PATCH / GET) — emails Tracy*

### `POST /bills/generate`  → **201** → status `PENDING`, **bill notification emailed to Tracy**
```json
{ "meterId": 1, "month": 5, "year": 2026, "dueInDays": 15 }
```
Note the `billReference` (e.g. `BILL-2026-05-000001`).
### `PATCH /bills/1/approve`  → **200** → `APPROVED`
### `GET /bills`, `GET /bills/1`, `GET /bills/reference/BILL-2026-05-000001`, `GET /bills/customer/1`  → **200**
### `POST /bills/apply-overdue`  → **200** (no-op unless past due; exercises the endpoint)

---

# PART 7 — Payments (FINANCE/ADMIN)  *(POST / GET) — emails Tracy on full payment*

### `POST /payments` — partial  → **201** → bill `PARTIALLY_PAID`
```json
{ "billReference": "BILL-2026-05-000001", "amountPaid": 5000.00,
  "paymentMethod": "MOBILE_MONEY", "paymentDate": "2026-06-05" }
```
### `POST /payments` — pay the remaining `outstandingBalance`  → **201** → bill `PAID`,
**payment-confirmation emailed to Tracy**.
### `GET /payments`, `GET /payments/bill/BILL-2026-05-000001`, `GET /payments/customer/1`  → **200**

---

# PART 8 — Notifications  *(GET / PATCH)*

### `GET /notifications`  → **200** (bill + payment messages, status `SENT`)
### `GET /notifications/customer/1`  → **200** (Tracy's messages)
### `PATCH /notifications/1/sent`  → **200**

---

# PART 8b — Customer self-service (use **Tracy's token**)  *(the core "view my data" feature)*

Re-**Authorize** with Tracy's token (from PART 1), then:
### `GET /customers/me`  → **200** — Tracy's own profile (phone shows `+250788111001`)
### `GET /bills/my`  → **200** — only Tracy's bills
### `GET /payments/my`  → **200** — only Tracy's payment history
### `GET /notifications/my`  → **200** — only Tracy's notifications

> A customer **cannot** reach the staff lookups — with Tracy's token:
> `GET /bills/customer/1` → **403**, `GET /bills/1` → **403**, `GET /payments` → **403**.
> Customers only ever see their own data through the `/my` and `/me` endpoints.

---

# PART 9 — Users (ADMIN)  *(GET / PATCH)*

### `GET /users`  → **200** ; `GET /users/1`  → **200**
### `PATCH /users/{id}/status?status=INACTIVE`  → **200** (deactivate), then `?status=ACTIVE` to restore.

---

# PART 10 — Validation & NO-DUPLICATE checks (must fail as shown)

| Action | Expected |
|--------|----------|
| `POST /customers` with the SAME `nationalId` as Tracy (`1199900000000001`) | **409** Conflict |
| `POST /customers` with the SAME email as John | **409** Conflict |
| `POST /customers` with empty `fullNames`, `email:"bad"`, `phoneNumber:"abc"` | **400** + `fieldErrors` |
| `POST /meters` reusing `MTR-EL-0001` | **409** Conflict |
| `POST /meters` with `installationDate` in the future (e.g. `2099-01-01`) | **400** |
| `POST /auth/signup` password `abc` (weak) | **400** (policy) |
| `POST /readings` with `currentReading` ≤ previous | **422** |
| `POST /readings` where `readingDate` month/year ≠ `month`/`year` (e.g. date `2026-05-31`, `month:8`) | **422** |
| `POST /readings` duplicate meter 1 / month 5 / 2026 | **409** Conflict |
| `POST /bills/generate` again for meter 1 / 5 / 2026 | **409** Conflict |
| `POST /payments` amount `99999999` (over balance) | **422** |
| `POST /payments` on a still-`PENDING` bill | **422** |
| Tracy's token → `POST /customers` | **403** Forbidden |
| **ADMIN** token → `POST /readings` (only OPERATOR may capture) | **403** Forbidden |
| Tracy's token → `GET /bills/customer/1` or `GET /bills/1` (staff-only) | **403** Forbidden |
| any secured call with **Authorize → Logout** | **401** |
| `GET /customers/9999` | **404** |

Every error returns the standard envelope:
```json
{ "timestamp":"...", "status":409, "error":"Conflict",
  "message":"Customer already exists with National ID: 1199900000000001",
  "path":"/api/v1/customers", "fieldErrors": null }
```

---

# PART 11 — Task 6: Database routines (verify in the DATABASE, not Swagger)

Task 6 is **database-level** (trigger + stored procedure + cursor), so it's verified in
PostgreSQL with `psql`, not through Swagger. This is exactly what was run and confirmed working.

**Step 1 — load the routines once** (after the app has created the tables):
```bash
# Windows: set the password first, then run psql
set PGPASSWORD=1234
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -d utility_billing -f src/main/resources/db/postgres_routines.sql
```

**Step 2 — confirm they exist:**
```sql
SELECT tgname FROM pg_trigger WHERE tgname LIKE 'trg_bill%';           -- 2 triggers
SELECT proname, prokind FROM pg_proc
 WHERE proname IN ('fn_bill_after_insert','fn_bill_after_update','sp_apply_overdue_penalties');
```

**Step 3 — prove the AFTER INSERT trigger** (insert a bill via SQL → a notification appears):
```sql
SELECT count(*) FROM notifications;                       -- note the number
INSERT INTO bills (bill_reference, customer_id, meter_id, bill_month, bill_year, consumption,
  tariff_amount, service_charge, tax_amount, penalty_amount, total_amount, amount_paid,
  outstanding_balance, due_date, status, created_at, updated_at)
SELECT 'TRG-DEMO-12-2027', m.customer_id, m.id, 12, 2027, 50, 17000,1000,3240,0,21240,0,21240,
  CURRENT_DATE - 5, 'APPROVED', NOW(), NOW()
FROM meters m WHERE m.customer_id IS NOT NULL LIMIT 1;
SELECT message, status FROM notifications ORDER BY id DESC LIMIT 1;     -- count +1, "...successfully processed."
```

**Step 4 — prove the stored procedure + cursor** (overdue penalty):
```sql
CALL sp_apply_overdue_penalties(5.00);                    -- cursor walks overdue bills, adds 5%
SELECT bill_reference, status, penalty_amount, outstanding_balance
  FROM bills WHERE bill_reference='TRG-DEMO-12-2027';     -- status OVERDUE, penalty 1062.00
```

**Step 5 — prove the AFTER UPDATE trigger** (full payment → notify):
```sql
UPDATE bills SET status='PAID', amount_paid=total_amount, outstanding_balance=0
  WHERE bill_reference='TRG-DEMO-12-2027';
SELECT message, status FROM notifications ORDER BY id DESC LIMIT 1;     -- "...fully paid. Thank you." (SENT)
```

**Clean up the demo rows (optional):**
```sql
DELETE FROM notifications WHERE bill_id=(SELECT id FROM bills WHERE bill_reference='TRG-DEMO-12-2027');
DELETE FROM bills WHERE bill_reference='TRG-DEMO-12-2027';
```

> ⚠️ **Avoid double notifications:** the running Java app *also* inserts a notification (and sends
> the email). If you keep these triggers installed **and** run the live app against the same
> Postgres DB, each bill/payment produces **two** notification rows. For grading, demonstrate
> Task 6 with the psql steps above; before running the live app on that DB, drop the triggers:
> ```sql
> DROP TRIGGER IF EXISTS trg_bill_after_insert ON bills;
> DROP TRIGGER IF EXISTS trg_bill_after_update ON bills;
> ```
> (The Java layer keeps notifications working on any database — including H2 — and also emails them.)

---

## Endpoint coverage checklist (all hit above)
- **Auth:** signup ✓ verify-account ✓ login ✓ verify-otp ✓ forgot-password ✓ reset-password ✓
- **Users:** GET list ✓ GET id ✓ PATCH status ✓
- **Customers:** POST ✓ GET ✓ GET id ✓ **GET /me (customer)** ✓ PUT ✓ PATCH status (activate/deactivate) ✓
- **Meters:** POST ✓ PUT ✓ GET ✓ GET id ✓ GET by-customer ✓ claim ✓
- **Readings:** POST (OPERATOR only) ✓ GET ✓ GET id ✓ GET by-meter ✓
- **Config:** POST tariff/tax/penalty ✓ GET tariffs/tariff-id/taxes/penalties ✓
- **Bills:** POST generate ✓ PATCH approve ✓ POST apply-overdue ✓ GET ✓ GET id ✓ GET reference ✓ GET by-customer ✓ **GET /my (customer)** ✓
- **Payments:** POST ✓ GET ✓ GET by-bill ✓ GET by-customer ✓ **GET /my (customer)** ✓
- **Notifications:** GET ✓ GET by-customer ✓ **GET /my (customer)** ✓ PATCH sent ✓

> `forgot-password` / `reset-password`: run them against Tracy
> (`POST /auth/forgot-password {"email":"tracytesi69@gmail.com"}` → reset OTP emailed →
> `POST /auth/reset-password {email, otp, newPassword:"Tracy456!"}` → **200**).
