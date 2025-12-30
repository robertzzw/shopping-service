drop PROCEDURE if exists `update_user_balance_and_return`;
DELIMITER $$
CREATE PROCEDURE `update_user_balance_and_return`(
    IN userId BIGINT,
    IN amount INT,
    IN operation_type VARCHAR(10)  -- 'decrease' 或 'increase'
)
proc_label:BEGIN
    DECLARE affected_rows INT DEFAULT 0;
    DECLARE result_message VARCHAR(10) DEFAULT 'failure';
    if operation_type = 'decrease' then
        UPDATE `user`
        SET account_balance = account_balance - amount,
            balance_version = balance_version + 1
        WHERE user_id = userId
          AND account_balance >= amount;

    elseif operation_type = 'increase' then
        UPDATE `user`
        SET account_balance = account_balance + amount,
            balance_version = balance_version + 1
        WHERE user_id = userId;
    else
        SELECT u.*, result_message FROM `user` u WHERE 1=0;
        leave proc_label;
    END IF;

    -- 获取影响行数
    SET affected_rows = ROW_COUNT();
    IF affected_rows = 1 THEN
        SET result_message = 'success';
    ELSE
        SET result_message = 'failure';
    END IF;
    SELECT u.*, result_message FROM `user` u WHERE u.user_id = userId;
END$$
DELIMITER ;