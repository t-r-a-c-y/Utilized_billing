-- =============================================================================
--  Utility Billing System - PostgreSQL Database Routines
--  Trigger + Stored Procedure + Cursor (requirement #8)
--
--  Run AFTER the application has created the tables once (ddl-auto=update):
--     psql -U postgres -d utility_billing -f postgres_routines.sql
--
--  Behaviour:
--    * On bill generation (INSERT into bills)  -> insert a PENDING notification.
--    * On full payment    (status -> PAID)     -> insert a SENT notification.
--    * sp_apply_overdue_penalties() uses an explicit CURSOR to flag overdue
--      bills and add a late-payment penalty.
--
--  NOTE: the Java NotificationService already creates these notifications. If you
--  want the DATABASE to own that behaviour instead, keep these triggers and
--  remove the notificationService.createForBill(...) calls to avoid duplicates.
-- =============================================================================

-- ----------------------------------------------------------------------------
-- 1) TRIGGER: after a bill is inserted -> queue a "bill processed" notification
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_bill_after_insert()
RETURNS TRIGGER AS $$
DECLARE
    v_name VARCHAR(255);
BEGIN
    SELECT full_names INTO v_name FROM customers WHERE id = NEW.customer_id;

    INSERT INTO notifications (customer_id, bill_id, message, status, created_at, updated_at)
    VALUES (
        NEW.customer_id,
        NEW.id,
        'Dear ' || v_name || ',' || chr(10) ||
        'Your ' || LPAD(NEW.bill_month::text, 2, '0') || '/' || NEW.bill_year ||
        ' utility bill of ' || to_char(NEW.total_amount, 'FM999999990.00') ||
        ' FRW has been successfully processed.',
        'PENDING',
        NOW(), NOW()
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_after_insert ON bills;
CREATE TRIGGER trg_bill_after_insert
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION fn_bill_after_insert();

-- ----------------------------------------------------------------------------
-- 2) TRIGGER: when a bill becomes PAID -> queue a "fully paid" notification
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_bill_after_update()
RETURNS TRIGGER AS $$
DECLARE
    v_name VARCHAR(255);
BEGIN
    IF NEW.status = 'PAID' AND OLD.status <> 'PAID' THEN
        SELECT full_names INTO v_name FROM customers WHERE id = NEW.customer_id;

        INSERT INTO notifications (customer_id, bill_id, message, status, created_at, updated_at)
        VALUES (
            NEW.customer_id,
            NEW.id,
            'Dear ' || v_name || ',' || chr(10) ||
            'Your ' || LPAD(NEW.bill_month::text, 2, '0') || '/' || NEW.bill_year ||
            ' utility bill of ' || to_char(NEW.total_amount, 'FM999999990.00') ||
            ' FRW has been fully paid. Thank you.',
            'SENT',
            NOW(), NOW()
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_after_update ON bills;
CREATE TRIGGER trg_bill_after_update
    AFTER UPDATE ON bills
    FOR EACH ROW
    EXECUTE FUNCTION fn_bill_after_update();

-- ----------------------------------------------------------------------------
-- 3) STORED PROCEDURE + CURSOR: flag overdue bills and apply a late penalty
-- ----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_apply_overdue_penalties(p_penalty_pct NUMERIC)
LANGUAGE plpgsql AS $$
DECLARE
    -- explicit cursor over overdue, still-owing bills
    cur CURSOR FOR
        SELECT id, outstanding_balance
        FROM bills
        WHERE outstanding_balance > 0
          AND due_date < CURRENT_DATE
          AND status IN ('APPROVED', 'PARTIALLY_PAID');
    v_id          BIGINT;
    v_outstanding NUMERIC(14,2);
    v_penalty     NUMERIC(14,2);
BEGIN
    OPEN cur;
    LOOP
        FETCH cur INTO v_id, v_outstanding;
        EXIT WHEN NOT FOUND;

        v_penalty := ROUND(v_outstanding * p_penalty_pct / 100, 2);

        UPDATE bills
        SET penalty_amount      = penalty_amount + v_penalty,
            total_amount        = total_amount + v_penalty,
            outstanding_balance = outstanding_balance + v_penalty,
            status              = 'OVERDUE',
            updated_at          = NOW()
        WHERE id = v_id;
    END LOOP;
    CLOSE cur;
END;
$$;

-- To run the procedure (apply a 5% penalty to all overdue bills):
--   CALL sp_apply_overdue_penalties(5.00);
