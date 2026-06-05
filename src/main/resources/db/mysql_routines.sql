-- =============================================================================
--  Utility Billing System - MySQL Database Routines
--  Trigger + Stored Procedure + Cursor (requirement #8)
--
--  Run this AFTER the application has created the tables once (ddl-auto=update),
--  e.g.  mysql -u root -p utility_billing < mysql_routines.sql
--
--  These routines implement the required DB-side behavior:
--    * On bill generation (INSERT into bills) -> insert a PENDING notification.
--    * On full payment (bills.status updated to PAID) -> insert a SENT notification.
--  The Java services also create notifications; when relying on the DB triggers
--  you would disable the Java NotificationService call to avoid duplicates. Both
--  are provided so the project demonstrates each approach.
-- =============================================================================

DELIMITER $$

-- ----------------------------------------------------------------------------
-- 1) TRIGGER: after a bill is inserted, queue a "bill processed" notification.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_bill_after_insert $$
CREATE TRIGGER trg_bill_after_insert
AFTER INSERT ON bills
FOR EACH ROW
BEGIN
    DECLARE v_name VARCHAR(255);

    SELECT full_names INTO v_name FROM customers WHERE id = NEW.customer_id;

    INSERT INTO notifications (customer_id, bill_id, message, status, created_at, updated_at)
    VALUES (
        NEW.customer_id,
        NEW.id,
        CONCAT('Dear ', v_name, ',\nYour ',
               LPAD(NEW.bill_month, 2, '0'), '/', NEW.bill_year,
               ' utility bill of ', FORMAT(NEW.total_amount, 2),
               ' FRW has been successfully processed.'),
        'PENDING',
        NOW(),
        NOW()
    );
END $$

-- ----------------------------------------------------------------------------
-- 2) TRIGGER: when a bill transitions to PAID, queue a "fully paid" notification.
-- ----------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_bill_after_update $$
CREATE TRIGGER trg_bill_after_update
AFTER UPDATE ON bills
FOR EACH ROW
BEGIN
    DECLARE v_name VARCHAR(255);

    IF NEW.status = 'PAID' AND OLD.status <> 'PAID' THEN
        SELECT full_names INTO v_name FROM customers WHERE id = NEW.customer_id;

        INSERT INTO notifications (customer_id, bill_id, message, status, created_at, updated_at)
        VALUES (
            NEW.customer_id,
            NEW.id,
            CONCAT('Dear ', v_name, ',\nYour ',
                   LPAD(NEW.bill_month, 2, '0'), '/', NEW.bill_year,
                   ' utility bill of ', FORMAT(NEW.total_amount, 2),
                   ' FRW has been fully paid. Thank you.'),
            'SENT',
            NOW(),
            NOW()
        );
    END IF;
END $$

-- ----------------------------------------------------------------------------
-- 3) STORED PROCEDURE + CURSOR: flag overdue bills and apply a late penalty.
--    Iterates over all unpaid bills past their due date using an explicit cursor.
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_apply_overdue_penalties $$
CREATE PROCEDURE sp_apply_overdue_penalties(IN p_penalty_pct DECIMAL(6,2))
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_bill_id BIGINT;
    DECLARE v_outstanding DECIMAL(14,2);
    DECLARE v_penalty DECIMAL(14,2);

    -- Cursor over overdue, still-owing bills.
    DECLARE cur CURSOR FOR
        SELECT id, outstanding_balance
        FROM bills
        WHERE outstanding_balance > 0
          AND due_date < CURDATE()
          AND status IN ('APPROVED', 'PARTIALLY_PAID');

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_bill_id, v_outstanding;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;

        SET v_penalty = ROUND(v_outstanding * p_penalty_pct / 100, 2);

        UPDATE bills
        SET penalty_amount      = penalty_amount + v_penalty,
            total_amount        = total_amount + v_penalty,
            outstanding_balance = outstanding_balance + v_penalty,
            status              = 'OVERDUE',
            updated_at          = NOW()
        WHERE id = v_bill_id;
    END LOOP;
    CLOSE cur;
END $$

DELIMITER ;

-- To run the procedure (e.g. apply a 5% penalty):
--   CALL sp_apply_overdue_penalties(5.00);
