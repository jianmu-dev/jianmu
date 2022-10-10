ALTER TABLE `workflow_instance`
    add `occurred_time` datetime DEFAULT NULL COMMENT '触发时间' AFTER `workflow_version`;
ALTER TABLE `jm_project_last_execution`
    add `occurred_time` datetime DEFAULT NULL COMMENT '触发时间' AFTER `serial_no`;

UPDATE `workflow_instance` `w`
SET occurred_time = (SELECT occurred_time FROM `jianmu_trigger_event` `o` WHERE `o`.`id` = `w`.`trigger_id`);

DELIMITER //
CREATE PROCEDURE `update_occurred_time`()
BEGIN
    DECLARE `i` int DEFAULT (0);
    DECLARE `ref` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE `instance_ref` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    DECLARE `w_count` int DEFAULT (0);
    DECLARE `instance_occurred_time` datetime;
    DECLARE `workflow_cursor` CURSOR FOR SELECT `workflow_ref` FROM `jm_project_last_execution`;
    DECLARE EXIT HANDLER FOR NOT FOUND SET `i` := 1;
    OPEN `workflow_cursor`;
    WHILE `i` = 0
        DO
            FETCH `workflow_cursor` INTO `ref`;
            BEGIN
                select count(*)
                into `w_count`
                FROM `workflow_instance`
                WHERE `workflow_ref` = `ref`;
                IF `w_count` > 0 THEN
                    SELECT `workflow_ref`, `occurred_time`
                    INTO `instance_ref`, `instance_occurred_time`
                    FROM `workflow_instance`
                    WHERE `workflow_ref` = `ref`
                    ORDER BY `serial_no` DESC
                    LIMIT 1;
                    UPDATE `jm_project_last_execution`
                    SET `occurred_time` = `instance_occurred_time`
                    WHERE `workflow_ref` = `instance_ref`;
                END IF;
            END;
        END WHILE;
    CLOSE `workflow_cursor`;
END //
DELIMITER ;

CALL `update_occurred_time`;
DROP PROCEDURE `update_occurred_time`;