CREATE TABLE `volume`
(
    `id`             varchar(45) NOT NULL COMMENT 'ID',
    `name`           varchar(45) DEFAULT NULL COMMENT 'Volume名称',
    `scope`          varchar(45) DEFAULT NULL COMMENT '使用范围',
    `worker_id`      varchar(45) DEFAULT NULL COMMENT 'Worker ID',
    `project_id`     varchar(45) DEFAULT NULL COMMENT 'Project ID',
    `available`      bit(1)      NOT NULL COMMENT '是否可用',
    `taint`          bit(1)      NOT NULL COMMENT '是否被污染，无法删除',
    `created_time`   datetime    NOT NULL COMMENT '创建时间',
    `available_time` datetime    DEFAULT NULL COMMENT '可用时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_name_project_id` (`name`, `project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Volume表';

UPDATE `jianmu_trigger`
SET `webhook` = null
where `type` = 'CRON';
UPDATE `jianmu_trigger`
SET `schedule` = null
where `type` = 'WEBHOOK';

ALTER TABLE `task_instance`
    ADD INDEX `idx_businessid` (`business_id`);
ALTER TABLE `task_instance`
    ADD INDEX `idx_triggerid` (`trigger_id`);
ALTER TABLE `task_instance`
    ADD INDEX `idx_workerid` (`worker_id`);
ALTER TABLE `async_task_instance`
    ADD INDEX `idx_workflowinstanceid` (`workflow_instance_id`);
ALTER TABLE `workflow_instance`
    ADD INDEX `idx_workflowref` (`workflow_ref`);
ALTER TABLE `task_instance_parameter`
    ADD INDEX `idx_instance_id` (`instance_id`);
ALTER TABLE `task_instance_parameter`
    ADD INDEX `idx_trigger_id` (`trigger_id`);
