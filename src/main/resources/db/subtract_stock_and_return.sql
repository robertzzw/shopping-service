drop PROCEDURE if exists `subtract_stock_and_return`;
DELIMITER $$
CREATE PROCEDURE `subtract_stock_and_return`(
    IN skuId BIGINT,
    IN quantity INT,
    IN version BIGINT
)
BEGIN
    DECLARE affected_rows INT DEFAULT 0;
    DECLARE result_message VARCHAR(10) DEFAULT 'failure';

    UPDATE product_sku
    SET stock_quantity = stock_quantity - quantity,
        stock_version = stock_version + 1
    WHERE sku_id = skuId
      AND stock_quantity >= quantity
      and stock_version = version;

    -- 获取影响行数
    SET affected_rows = ROW_COUNT();
    IF affected_rows = 1 THEN
        SET result_message = 'success';
    ELSE
        SET result_message = 'failure';
    END IF;
    SELECT sku.*, result_message FROM product_sku sku WHERE sku.sku_id = skuId;
END$$
DELIMITER ;
