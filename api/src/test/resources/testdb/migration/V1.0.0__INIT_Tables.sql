CREATE TABLE `jianmu_project`
(
    `id`                 varchar(45)  NOT NULL COMMENT 'ID',
    `git_repo_id`        varchar(150) NOT NULL COMMENT 'Git仓库ID',
    `workflow_name`      varchar(45)  NOT NULL COMMENT '流程定义显示名称',
    `workflow_ref`       varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version`   varchar(45)  NOT NULL COMMENT '流程定义版本',
    `steps`              int          NOT NULL COMMENT '步骤数量',
    `dsl_text`           longtext     NOT NULL COMMENT 'DSL内容文本',
    `created_time`       datetime    DEFAULT NULL COMMENT '创建时间',
    `last_modified_by`   varchar(45) DEFAULT NULL COMMENT '最后修改人',
    `last_modified_time` datetime     NOT NULL COMMENT '最后修改时间',
    PRIMARY KEY (`id`)
);

CREATE TABLE `jianmu`.`git_repo`
(
    `id`                    VARCHAR(45)  NOT NULL COMMENT 'ID',
    `uri`                   VARCHAR(100) NULL COMMENT '仓库URI',
    `type`                  VARCHAR(45)  NULL COMMENT '认证类型',
    `https_username`        VARCHAR(45)  NULL COMMENT 'https用户名',
    `https_password`        VARCHAR(45)  NULL COMMENT 'https密码',
    `private_key`           TEXT         NULL COMMENT 'ssh方式私钥',
    `branch`                VARCHAR(45)  NULL COMMENT '分支名',
    `is_clone_all_branches` TINYINT(1)   NULL COMMENT '是否Clone全部分支',
    `dsl_path`              VARCHAR(100) NULL COMMENT 'dsl文件路径',
    PRIMARY KEY (`id`)
);

CREATE TABLE `dsl_source_code`
(
    `project_id`         varchar(255) NOT NULL COMMENT '项目ID',
    `workflow_ref`       varchar(45)  NOT NULL COMMENT '关联流程定义Ref',
    `workflow_version`   varchar(45)  NOT NULL COMMENT '关联流程定义版本',
    `dsl_text`           longtext     NOT NULL COMMENT '原始DSL文本',
    `created_time`       datetime     NOT NULL COMMENT '创建时间',
    `last_modified_by`   varchar(45) DEFAULT NULL COMMENT '最后修改者',
    `last_modified_time` datetime    DEFAULT NULL COMMENT '最后修改时间'
);

CREATE TABLE `input_parameter`
(
    `def_key`          varchar(45)  NOT NULL COMMENT '任务定义Key, 表示任务定义类型',
    `async_task_ref`   varchar(45)  NOT NULL COMMENT '流程定义上下文中的AsyncTask唯一标识',
    `workflow_ref`     varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version` varchar(45)  NOT NULL COMMENT '流程定义版本',
    `project_id`       varchar(100) NOT NULL COMMENT '项目ID',
    `ref`              varchar(45)  NOT NULL COMMENT '参数唯一引用名称',
    `parameter_id`     varchar(45)  NOT NULL COMMENT '参数引用Id'
);

CREATE TABLE `workflow`
(
    `ref_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程定义标识，主键',
    `ref`         varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '唯一引用名称',
    `version`     varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本',
    `name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `nodes`       longblob COMMENT 'Node列表',
    PRIMARY KEY (`ref_version`)
);

CREATE TABLE `workflow_instance`
(
    `id`               varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '唯一ID主键',
    `trigger_id`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器ID',
    `name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
    `description`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `run_mode`         varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运行模式',
    `status`           varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运行状态',
    `workflow_ref`     varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程定义唯一引用名称',
    `workflow_version` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程定义版本',
    `task_instances`   blob COMMENT '任务实例列表',
    `start_time`       datetime DEFAULT NULL COMMENT '开始时间',
    `end_time`         datetime DEFAULT NULL COMMENT '结束时间',
    `_version`         int NOT NULL COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`)
);

CREATE TABLE `task_definition`
(
    `id`                 varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID',
    `ref`                varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义唯一引用',
    `name`               varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
    `created_time`       datetime NOT NULL COMMENT '创建时间',
    `last_modified_time` datetime NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
);

CREATE TABLE `task_definition_version`
(
    `task_definition_id`  varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义ID',
    `name`                varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本名称',
    `task_definition_ref` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义唯一引用',
    `definition_key`      varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义唯一Key',
    `description`         varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `created_time`        datetime NOT NULL COMMENT '创建时间',
    `last_modified_time`  datetime NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`definition_key`)
);

CREATE TABLE `parameter_refer`
(
    `workflow_ref`         varchar(45) NOT NULL COMMENT '流程定义Ref',
    `workflow_version`     varchar(45) NOT NULL COMMENT '流程定义版本',
    `source_task_ref`      varchar(45) NOT NULL COMMENT '源任务(输出的任务)Ref',
    `source_parameter_ref` varchar(45) NOT NULL COMMENT '源参数Ref',
    `target_task_ref`      varchar(45) NOT NULL COMMENT '目标任务(引用的任务)Ref',
    `target_parameter_ref` varchar(45) NOT NULL COMMENT '目标参数Ref'
);

CREATE TABLE `task_instance`
(
    `id`                varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
    `serial_no`         int      DEFAULT NULL COMMENT '执行序号',
    `def_key`           varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义唯一Key',
    `async_task_ref`    varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程定义上下文中的AsyncTask唯一标识',
    `workflow_ref`      varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义Ref',
    `workflow_version`  varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义版本',
    `business_id`       varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '外部业务ID',
    `project_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目ID',
    `start_time`        datetime DEFAULT NULL COMMENT '开始时间',
    `end_time`          datetime DEFAULT NULL COMMENT '结束时间',
    `result_file`       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '执行结果文件',
    `status`            varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务运行状态',
    `output_parameters` blob COMMENT '任务实例输出参数',
    PRIMARY KEY (`id`)
);

CREATE TABLE `task_instance_parameter`
(
    `instance_id`    varchar(45)  NOT NULL COMMENT '任务实例ID',
    `serial_no`      int          NOT NULL COMMENT '执行序号',
    `def_key`        varchar(45)  NOT NULL COMMENT '任务定义Key（类型）',
    `async_task_ref` varchar(45)  NOT NULL COMMENT '任务节点ref',
    `business_id`    varchar(45)  NOT NULL COMMENT '流程实例ID',
    `project_id`     varchar(255) NOT NULL COMMENT '项目ID',
    `ref`            varchar(45)  NOT NULL COMMENT '参数ref',
    `type`           varchar(45)  NOT NULL COMMENT '参数类型',
    `parameter_id`   varchar(45)  NOT NULL COMMENT '参数引用ID'
);

CREATE TABLE `parameter`
(
    `id`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数ID',
    `type`  varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型',
    `value` blob NOT NULL COMMENT '参数值',
    PRIMARY KEY (`id`)
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
    `name`               varchar(100) NOT NULL COMMENT '名称',
    `description`        varchar(255) DEFAULT NULL COMMENT '描述',
    `created_time`       datetime     NOT NULL COMMENT '创建时间',
    `last_modified_time` datetime     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`name`)
);

CREATE TABLE `secret_kv_pair`
(
    `namespace_name` varchar(100) NOT NULL COMMENT '命名空间名称',
    `kv_key`         varchar(45)  NOT NULL COMMENT '参数key',
    `kv_value`       text         NOT NULL COMMENT '参数值'
);