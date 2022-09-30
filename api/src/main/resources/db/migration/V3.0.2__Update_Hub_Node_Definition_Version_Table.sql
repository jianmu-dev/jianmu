UPDATE `jm_hub_node_definition_version`
SET `input_parameters`  = Replace(`input_parameters`, 'java.util.HashSet', 'java.util.ArrayList'),
    `output_parameters` = Replace(`output_parameters`, 'java.util.HashSet', 'java.util.ArrayList');

ALTER TABLE `jm_project_last_execution`
    ADD COLUMN `workflow_instance_id` varchar(45) DEFAULT NULL COMMENT '流程实例ID' AFTER `workflow_ref`;
ALTER TABLE `jm_project_last_execution`
    ADD COLUMN `serial_no` int DEFAULT NULL COMMENT '序号' AFTER `workflow_instance_id`;

DELIMITER //
CREATE PROCEDURE `update_project_last_execution`()
BEGIN
    DECLARE `i` int DEFAULT (0);
    DECLARE `w_count` int DEFAULT (0);
    DECLARE `ref` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE `instance_workflow_ref` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    DECLARE `instance_id` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    DECLARE `instance_no` int;
    DECLARE `project_cursor` CURSOR FOR SELECT `workflow_ref` FROM `jm_project`;
    DECLARE EXIT HANDLER FOR NOT FOUND SET `i` := 1;
    OPEN `project_cursor`;
    WHILE `i` = 0
        DO
            FETCH `project_cursor` INTO `ref`;
            BEGIN
                select count(*)
                into `w_count`
                FROM `jm_workflow_instance`
                WHERE `workflow_ref` = `ref`;
                IF `w_count` > 0 THEN
                    SELECT `id`, `serial_no`, `workflow_ref`
                    INTO `instance_id`, `instance_no`, `instance_workflow_ref`
                    FROM `jm_workflow_instance`
                    WHERE `workflow_ref` = `ref`
                    ORDER BY serial_no DESC
                    LIMIT 1;
                    UPDATE `jm_project_last_execution`
                    SET `workflow_instance_id` = `instance_id`,
                        `serial_no`            = `instance_no`
                    WHERE `workflow_ref` = `instance_workflow_ref`;
                END IF;
            END;
        END WHILE;
    CLOSE `project_cursor`;
END //
DELIMITER ;

CALL `update_project_last_execution`;
DROP PROCEDURE `update_project_last_execution`;