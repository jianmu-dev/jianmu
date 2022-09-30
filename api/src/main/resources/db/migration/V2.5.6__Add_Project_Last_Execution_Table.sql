UPDATE `hub_node_definition_version`
SET `input_parameters`  = Replace(`input_parameters`, 'java.util.HashSet', 'java.util.ArrayList'),
    `output_parameters` = Replace(`output_parameters`, 'java.util.HashSet', 'java.util.ArrayList');

CREATE TABLE `jm_project_last_execution`
(
    `workflow_ref`         varchar(45) NOT NULL COMMENT 'ID',
    `workflow_instance_id` varchar(45) DEFAULT NULL COMMENT '流程实例ID',
    `serial_no`            int         DEFAULT NULL COMMENT '流程实例序号',
    `status`               varchar(45) DEFAULT NULL COMMENT '运行状态',
    `start_time`           datetime    DEFAULT NULL COMMENT '开始时间',
    `suspended_time`       datetime    DEFAULT NULL COMMENT '挂起时间',
    `end_time`             datetime    DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`workflow_ref`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='项目最后执行记录';

DELIMITER //
CREATE PROCEDURE `add_project_last_execution`()
BEGIN
    DECLARE `i` int DEFAULT (0);
    DECLARE `w_count` int DEFAULT (0);
    DECLARE `ref` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE `version` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    DECLARE `instance_id` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    DECLARE `instance_no` int;
    DECLARE `instance_start_time` datetime;
    DECLARE `instance_suspend_time` datetime;
    DECLARE `instance_end_time` datetime;
    DECLARE `instance_status` VARCHAR(45) character SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
    DECLARE `project_cursor` CURSOR FOR SELECT `workflow_ref`, `workflow_version` FROM `jianmu_project`;
    DECLARE EXIT HANDLER FOR NOT FOUND SET `i` := 1;
    OPEN `project_cursor`;
    WHILE `i` = 0
        DO
            FETCH `project_cursor` INTO `ref`, `version`;
            BEGIN
                select count(*)
                into `w_count`
                FROM `workflow_instance`
                WHERE `workflow_ref` = `ref`
                  AND `workflow_version` = `version`;
                IF `w_count` > 0 THEN
                    SELECT `id`, `serial_no`, `start_time`, `suspended_time`, `end_time`, `status`
                    INTO `instance_id`, `instance_no`, `instance_start_time`, `instance_suspend_time`, `instance_end_time`, `instance_status`
                    FROM `workflow_instance`
                    WHERE `workflow_ref` = `ref`
                      AND `workflow_version` = `version`
                    ORDER BY serial_no DESC
                    LIMIT 1;
                    INSERT INTO `jm_project_last_execution`(`workflow_ref`, `workflow_instance_id`, `serial_no`,
                                                            `start_time`,
                                                            `suspended_time`, `end_time`, `status`)
                    VALUES (`ref`, `instance_id`, `instance_no`, `instance_start_time`,
                            `instance_suspend_time`,
                            `instance_end_time`, `instance_status`);
                ElSE
                    INSERT INTO `jm_project_last_execution`(`workflow_ref`) VALUES (`ref`);
                END IF;
            END;
        END WHILE;
    CLOSE `project_cursor`;
END //
DELIMITER ;

CALL `add_project_last_execution`;
DROP PROCEDURE `add_project_last_execution`;