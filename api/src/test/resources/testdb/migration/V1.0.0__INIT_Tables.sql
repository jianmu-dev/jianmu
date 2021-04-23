CREATE TABLE `jianmu_project`
(
    `id`                 varchar(45)  NOT NULL COMMENT 'ID',
    `dsl_url`            varchar(150) NOT NULL COMMENT 'DSL文件地址',
    `workflow_name`      varchar(45)  NOT NULL COMMENT '流程定义显示名称',
    `workflow_ref`       varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version`   varchar(45)  NOT NULL COMMENT '流程定义版本',
    `steps`              int          NOT NULL COMMENT '步骤数量',
    `dsl_text`           longtext     NOT NULL COMMENT 'DSL内容文本',
    `last_modified_by`   varchar(45) DEFAULT NULL COMMENT '最后修改人',
    `last_modified_time` datetime     NOT NULL COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `workflow_ref_UNIQUE` (`workflow_ref`)
);

CREATE TABLE `workflow`
(
    `ref_version` varchar(255) NOT NULL COMMENT '流程定义标识，主键',
    `ref`         varchar(45)  NOT NULL COMMENT '唯一引用名称',
    `version`     varchar(45)  NOT NULL COMMENT '版本',
    `name`        varchar(255) DEFAULT NULL COMMENT '显示名称',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `nodes`       longblob COMMENT 'Node列表',
    PRIMARY KEY (`ref_version`)
);

CREATE TABLE `workflow_instance`
(
    `id`               varchar(45)  NOT NULL COMMENT '唯一ID主键',
    `trigger_id`       varchar(255) NOT NULL COMMENT '触发器ID',
    `name`             varchar(255) DEFAULT NULL COMMENT '显示名称',
    `description`      varchar(255) DEFAULT NULL COMMENT '描述',
    `run_mode`         varchar(45)  NOT NULL COMMENT '运行模式',
    `status`           varchar(45)  NOT NULL COMMENT '运行状态',
    `workflow_ref`     varchar(45)  NOT NULL COMMENT '流程定义唯一引用名称',
    `workflow_version` varchar(45)  NOT NULL COMMENT '流程定义版本',
    `task_instances`   blob COMMENT '任务实例列表',
    `start_time`       datetime     DEFAULT NULL COMMENT '开始时间',
    `end_time`         datetime     DEFAULT NULL COMMENT '结束时间',
    `_version`         int          NOT NULL COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`)
);

CREATE TABLE `task_definition`
(
    `id`   varchar(45) NOT NULL COMMENT 'ID',
    `ref`  varchar(45) NOT NULL COMMENT '任务定义唯一引用',
    `name` varchar(45) NOT NULL COMMENT '名称',
    PRIMARY KEY (`id`)
);

CREATE TABLE `task_definition_version`
(
    `task_definition_id`  varchar(45) NOT NULL COMMENT '任务定义ID',
    `name`                varchar(45) NOT NULL COMMENT '版本名称',
    `task_definition_ref` varchar(45) NOT NULL COMMENT '任务定义唯一引用',
    `description`         varchar(45) NOT NULL COMMENT '描述',
    PRIMARY KEY (`task_definition_ref`)
);

CREATE TABLE `task_parameter`
(
    `definition_id` varchar(255) NOT NULL,
    `name`          varchar(45)  NOT NULL,
    `ref`           varchar(45)  NOT NULL,
    `type`          varchar(45)  NOT NULL,
    `description`   varchar(255) DEFAULT NULL,
    `parameterId`   varchar(45)  NOT NULL
);

CREATE TABLE `task_instance`
(
    `id`             varchar(45)  NOT NULL COMMENT '主键',
    `def_key`        varchar(45)  NOT NULL COMMENT '任务定义唯一Key',
    `async_task_ref` varchar(45)  NOT NULL COMMENT '流程定义上下文中的AsyncTask唯一标识',
    `business_id`    varchar(45)  NOT NULL COMMENT '外部业务ID',
    `trigger_id`     varchar(255) NOT NULL COMMENT '触发器ID',
    `start_time`     datetime DEFAULT NULL COMMENT '开始时间',
    `end_time`       datetime DEFAULT NULL COMMENT '结束时间',
    `status`         varchar(45)  NOT NULL COMMENT '任务运行状态',
    PRIMARY KEY (`id`)
);

CREATE TABLE `task_instance_parameter`
(
    `instance_id` varchar(100) NOT NULL COMMENT '任务实例ID',
    `name`        varchar(45)  NOT NULL COMMENT '参数显示名',
    `ref`         varchar(45)  NOT NULL COMMENT '参数名',
    `type`        varchar(45)  NOT NULL COMMENT '参数类型',
    `description` varchar(255) NOT NULL COMMENT '描述',
    `parameterId` varchar(45)  NOT NULL COMMENT '参数存储ID'
);

CREATE TABLE `parameter`
(
    `id`    varchar(50) NOT NULL COMMENT '参数ID',
    `type`  varchar(45) NOT NULL COMMENT '参数类型',
    `value` blob        NOT NULL COMMENT '参数值',
    PRIMARY KEY (`id`)
);

CREATE TABLE `reference`
(
    `context_id`          varchar(100) DEFAULT NULL,
    `linked_parameter_id` varchar(100) DEFAULT NULL COMMENT '被关联参数ID',
    `parameter_id`        varchar(100) DEFAULT NULL COMMENT '参数ID'
);

CREATE TABLE `worker`
(
    `id`     varchar(45) NOT NULL COMMENT 'ID',
    `name`   varchar(45) DEFAULT NULL COMMENT '名称',
    `status` varchar(45) DEFAULT NULL COMMENT '状态',
    `type`   varchar(45) DEFAULT NULL COMMENT '类型',
    PRIMARY KEY (`id`)
);

CREATE TABLE `trigger`
(
    `id`                 varchar(45) NOT NULL COMMENT 'ID',
    `workflow_id`        varchar(45) DEFAULT NULL COMMENT '流程定义ID',
    `task_definition_id` varchar(45) DEFAULT NULL COMMENT '任务定义ID',
    `workspace`          varchar(45) DEFAULT NULL COMMENT '工作空间',
    `type`               varchar(45) NOT NULL COMMENT '触发类型',
    `category`           varchar(45) NOT NULL COMMENT '流程或任务分类',
    PRIMARY KEY (`id`)
);

CREATE TABLE `trigger_parameter`
(
    `trigger_id`  varchar(255) NOT NULL COMMENT '触发器ID',
    `name`        varchar(45)  NOT NULL COMMENT '显示名称',
    `ref`         varchar(45)  NOT NULL COMMENT '唯一引用名称',
    `type`        varchar(45)  NOT NULL COMMENT '类型',
    `description` varchar(45) DEFAULT NULL COMMENT '描述',
    `parameterId` varchar(45)  NOT NULL COMMENT '参数引用Id'
);

CREATE TABLE `secret_namespace`
(
    `name`        varchar(100) NOT NULL COMMENT '名称',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`name`)
);

CREATE TABLE `secret_kv_pair`
(
    `namespace_name` varchar(100) NOT NULL COMMENT '命名空间名称',
    `kv_key`         varchar(45)  NOT NULL COMMENT '参数key',
    `kv_value`       varchar(45)  NOT NULL COMMENT '参数值'
);