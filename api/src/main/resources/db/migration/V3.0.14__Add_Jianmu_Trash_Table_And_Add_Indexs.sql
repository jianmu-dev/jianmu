ALTER TABLE `jm_parameter`
    add `default` bit(1) NOT NULL DEFAULT 1 COMMENT '是否为默认值';

ALTER TABLE `jm_trigger_event_parameter`
    ADD INDEX `idx_trigger_event_id` (`trigger_event_id`);
ALTER TABLE `jm_web_request`
    ADD INDEX `idx_project_id` (`project_id`);
ALTER TABLE `jm_task_instance`
    ADD INDEX `idx_workflow_ref` (`workflow_ref`);
ALTER TABLE `jm_async_task_instance`
    ADD INDEX `idx_workflow_ref` (`workflow_ref`);
ALTER TABLE `jm_trigger_event`
    ADD INDEX `idx_project_id` (`project_id`);
ALTER TABLE `jm_workflow`
    ADD INDEX `idx_ref` (`ref`);

CREATE TABLE `jm_trash_project`
(
    `id`                   varchar(45)  NOT NULL COMMENT 'ID',
    `dsl_source`           varchar(45)  DEFAULT NULL COMMENT 'DSL来源',
    `dsl_type`             varchar(45)  DEFAULT NULL COMMENT 'DSL 类型',
    `trigger_type`         varchar(45)  DEFAULT NULL COMMENT '触发类型',
    `git_repo_id`          varchar(150) NOT NULL COMMENT 'Git仓库ID',
    `workflow_name`        varchar(45)  NOT NULL COMMENT '流程定义显示名称',
    `workflow_ref`         varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version`     varchar(45)  NOT NULL COMMENT '流程定义版本',
    `steps`                int          NOT NULL COMMENT '步骤数量',
    `dsl_text`             longtext     NOT NULL COMMENT 'DSL内容文本',
    `created_time`         datetime     DEFAULT NULL COMMENT '创建时间',
    `last_modified_by`     varchar(45)  DEFAULT NULL COMMENT '最后修改人',
    `last_modified_time`   datetime     NOT NULL COMMENT '最后修改时间',
    `workflow_description` varchar(255) DEFAULT NULL COMMENT '描述',
    `enabled`              tinyint(1)   DEFAULT NULL COMMENT '项目是否可触发',
    `mutable`              tinyint(1)   DEFAULT NULL COMMENT '项目状态是否可变',
    `concurrent`           int          NOT NULL COMMENT '并发执行数',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='废弃项目表';