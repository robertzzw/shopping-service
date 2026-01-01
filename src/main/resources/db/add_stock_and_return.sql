DROP PROCEDURE IF EXISTS `add_stock_and_return`;
DELIMITER $$
CREATE PROCEDURE `add_stock_and_return`(
    IN skuId BIGINT,
    IN quantity INT
)
BEGIN
    DECLARE affected_rows INT DEFAULT 0;
    DECLARE result_message VARCHAR(20) DEFAULT 'failure';
    -- 增加库存，直接更新
    UPDATE product_sku
    SET stock_quantity = stock_quantity + quantity,
        stock_version = stock_version + 1
    WHERE sku_id = skuId;

    -- 获取影响行数
    SET affected_rows = ROW_COUNT();
    IF affected_rows = 1 THEN
        SET result_message = 'success';
    END IF;
    -- 返回更新后的数据
    SELECT sku.*, result_message FROM product_sku sku WHERE sku.sku_id = skuId;
END$$
DELIMITER ;