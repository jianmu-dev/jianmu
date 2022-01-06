CREATE TABLE IF NOT EXISTS `async_task_instance`
(
    `id`                   varchar(45)  NOT NULL,
    `trigger_id`           varchar(45)  NOT NULL COMMENT 'Trigger ID',
    `workflow_ref`         varchar(45)  NOT NULL COMMENT '流程Ref',
    `workflow_version`     varchar(45)  NOT NULL COMMENT '流程版本',
    `workflow_instance_id` varchar(45)  NOT NULL COMMENT '流程实例ID',
    `name`                 varchar(45)  NOT NULL COMMENT '名称',
    `description`          varchar(255) NOT NULL COMMENT '描述',
    `status`               varchar(45)  NOT NULL COMMENT '状态',
    `async_task_ref`       varchar(45)  NOT NULL COMMENT '任务定义Ref',
    `async_task_type`      varchar(45)  NOT NULL COMMENT '任务定义类型',
    `activating_time`      datetime     NOT NULL COMMENT '激活时间',
    `start_time`           datetime DEFAULT NULL COMMENT '开始时间',
    `end_time`             datetime DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='异步任务实例表';

ALTER TABLE `jianmu_web_request` ADD INDEX request_time (`request_time` desc);

ALTER TABLE `jianmu_project` ADD `enabled` tinyint(1) DEFAULT NULL COMMENT '项目是否可触发';
ALTER TABLE `jianmu_project` ADD `mutable` tinyint(1) DEFAULT NULL COMMENT '项目状态是否可变';
ALTER TABLE `jianmu_project` DROP `event_bridge_id`;