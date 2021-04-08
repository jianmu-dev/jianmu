CREATE TABLE `parameter`
(
    `id`             int          NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `name`           varchar(255) NOT NULL COMMENT '显示名称',
    `ref`            varchar(255) NOT NULL COMMENT '唯一引用名称',
    `scope_id`       varchar(255) NOT NULL COMMENT '参数作用域id',
    `scope_category` varchar(50)  NOT NULL COMMENT '参数作用域分类',
    `description`    text COMMENT '描述',
    `source_type`    varchar(50)  NOT NULL COMMENT '参数值来源类型',
    `source_value`   varchar(255) DEFAULT NULL COMMENT '参数来源值',
    `value`          blob COMMENT '参数值',
    `parameter_type` varchar(45)  NOT NULL COMMENT '参数类型',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_scope_ref` (`scope_id`, `scope_category`, `ref`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 17
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `workflow`
(
    `ref_version` varchar(255) NOT NULL COMMENT '流程定义标识，主键',
    `ref`         varchar(45)  NOT NULL COMMENT '唯一引用名称',
    `version`     varchar(45)  NOT NULL COMMENT '版本',
    `name`        varchar(255) DEFAULT NULL COMMENT '显示名称',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `nodes`       longblob COMMENT 'Node列表',
    PRIMARY KEY (`ref_version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `workflow_instance`
(
    `id`               varchar(45) NOT NULL COMMENT '唯一ID主键',
    `name`             varchar(255) DEFAULT NULL COMMENT '显示名称',
    `description`      varchar(255) DEFAULT NULL COMMENT '描述',
    `run_mode`         varchar(45) NOT NULL COMMENT '运行模式',
    `status`           varchar(45) NOT NULL COMMENT '运行状态',
    `workflow_ref`     varchar(45) NOT NULL COMMENT '流程定义唯一引用名称',
    `workflow_version` varchar(45) NOT NULL COMMENT '流程定义版本',
    `task_instances`   blob COMMENT '任务实例列表',
    `start_time`       datetime     DEFAULT NULL COMMENT '开始时间',
    `end_time`         datetime     DEFAULT NULL COMMENT '结束时间',
    `_version`         int         NOT NULL COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `task_instance`
(
    `id`          varchar(45) NOT NULL COMMENT '主键',
    `key_version` varchar(45) NOT NULL COMMENT '辅助索引',
    `def_key`     varchar(45) NOT NULL COMMENT '任务定义唯一Key',
    `def_version` varchar(45) NOT NULL COMMENT '任务定义版本',
    `name`        varchar(45) NOT NULL COMMENT '显示名称',
    `description` varchar(45) DEFAULT NULL COMMENT '描述',
    `business_id` varchar(45) NOT NULL COMMENT '外部业务ID',
    `status`      varchar(45) NOT NULL COMMENT '任务运行状态',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务实例表';

CREATE TABLE `worker`
(
    `id`     varchar(45) NOT NULL COMMENT 'ID',
    `name`   varchar(45) DEFAULT NULL COMMENT '名称',
    `status` varchar(45) DEFAULT NULL COMMENT '状态',
    `type`   varchar(45) DEFAULT NULL COMMENT '类型',
    PRIMARY KEY (`id`)
);

insert into worker(id, name, status, type) values ('worker9527', 'Worker1', 'OFFLINE', 'DOCKER');