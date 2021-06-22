CREATE TABLE `jianmu_project`
(
    `id`                 varchar(45)  NOT NULL COMMENT 'ID',
    `dsl_source`         varchar(45) DEFAULT NULL COMMENT 'DSL来源',
    `git_repo_id`        varchar(150) NOT NULL COMMENT 'Git仓库ID',
    `workflow_name`      varchar(45)  NOT NULL COMMENT '流程定义显示名称',
    `workflow_ref`       varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version`   varchar(45)  NOT NULL COMMENT '流程定义版本',
    `steps`              int          NOT NULL COMMENT '步骤数量',
    `dsl_text`           longtext     NOT NULL COMMENT 'DSL内容文本',
    `created_time`       datetime    DEFAULT NULL COMMENT '创建时间',
    `last_modified_by`   varchar(45) DEFAULT NULL COMMENT '最后修改人',
    `last_modified_time` datetime     NOT NULL COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `workflow_ref_UNIQUE` (`workflow_ref`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='DSL表';

CREATE TABLE `cron_trigger`
(
    `id`         varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器ID',
    `project_id` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目ID',
    `corn`       varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Cron表达式',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='cron触发器表';

CREATE TABLE `quartz_trigger`
(
    `id`         int                                    NOT NULL COMMENT 'Quartz触发器ID',
    `trigger_id` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器ID',
    `cron`       varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Cron表达式',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Quartz触发器';

CREATE TABLE `git_repo`
(
    `id`                    varchar(45) NOT NULL COMMENT 'ID',
    `uri`                   varchar(100) DEFAULT NULL COMMENT '仓库URI',
    `type`                  varchar(45)  DEFAULT NULL COMMENT '认证类型',
    `credential`            blob COMMENT 'Git库凭据',
    `branch`                varchar(45)  DEFAULT NULL COMMENT '分支名',
    `is_clone_all_branches` tinyint(1)   DEFAULT NULL COMMENT '是否Clone全部分支',
    `dsl_path`              varchar(100) DEFAULT NULL COMMENT 'dsl文件路径',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Git仓库表';

CREATE TABLE `dsl_source_code`
(
    `project_id`         varchar(255) NOT NULL COMMENT '项目ID',
    `workflow_ref`       varchar(45)  NOT NULL COMMENT '关联流程定义Ref',
    `workflow_version`   varchar(45)  NOT NULL COMMENT '关联流程定义版本',
    `dsl_text`           longtext     NOT NULL COMMENT '原始DSL文本',
    `created_time`       datetime     NOT NULL COMMENT '创建时间',
    `last_modified_by`   varchar(45) DEFAULT NULL COMMENT '最后修改者',
    `last_modified_time` datetime    DEFAULT NULL COMMENT '最后修改时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='DSL源码表';

CREATE TABLE `input_parameter`
(
    `def_key`          varchar(45)  NOT NULL COMMENT '任务定义Key, 表示任务定义类型',
    `async_task_ref`   varchar(45)  NOT NULL COMMENT '流程定义上下文中的AsyncTask唯一标识',
    `workflow_ref`     varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version` varchar(45)  NOT NULL COMMENT '流程定义版本',
    `project_id`       varchar(100) NOT NULL COMMENT '项目ID',
    `ref`              varchar(45)  NOT NULL COMMENT '参数唯一引用名称',
    `parameter_id`     varchar(45)  NOT NULL COMMENT '参数引用Id'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='输入参数表';

CREATE TABLE `workflow`
(
    `ref_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程定义标识，主键',
    `ref`         varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '唯一引用名称',
    `version`     varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '版本',
    `name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `nodes`       longblob COMMENT 'Node列表',
    PRIMARY KEY (`ref_version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='流程定义表';

CREATE TABLE `workflow_instance`
(
    `id`               varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '唯一ID主键',
    `serial_no`        int                                                           NOT NULL COMMENT '执行顺序',
    `trigger_id`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器ID',
    `name`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
    `description`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `run_mode`         varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '运行模式',
    `status`           varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '运行状态',
    `workflow_ref`     varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '流程定义唯一引用名称',
    `workflow_version` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '流程定义版本',
    `task_instances`   blob COMMENT '任务实例列表',
    `start_time`       datetime                                                      DEFAULT NULL COMMENT '开始时间',
    `end_time`         datetime                                                      DEFAULT NULL COMMENT '结束时间',
    `_version`         int                                                           NOT NULL COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='流程实例表';

CREATE TABLE `task_definition`
(
    `ref`               varchar(255) NOT NULL COMMENT '唯一引用',
    `version`           varchar(45)  NOT NULL COMMENT '版本',
    `result_file`       varchar(45) DEFAULT NULL COMMENT '结果文件',
    `type`              varchar(45)  NOT NULL COMMENT '类型',
    `input_parameters`  blob COMMENT '输入参数',
    `output_parameters` blob COMMENT '输出参数',
    `meta_data`         blob COMMENT '元数据',
    `spec`              blob COMMENT '容器规格'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务定义表';

CREATE TABLE `task_definition_version`
(
    `task_definition_id`   varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义ID',
    `task_definition_name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义名称',
    `name`                 varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本名称',
    `task_definition_ref`  varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义唯一引用',
    `definition_key`       varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务定义唯一Key',
    `description`          varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `created_time`         datetime                                                     NOT NULL COMMENT '创建时间',
    `last_modified_time`   datetime                                                     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`definition_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务定义版本表';

CREATE TABLE `parameter_refer`
(
    `workflow_ref`         varchar(45) NOT NULL COMMENT '流程定义Ref',
    `workflow_version`     varchar(45) NOT NULL COMMENT '流程定义版本',
    `source_task_ref`      varchar(45) NOT NULL COMMENT '源任务(输出的任务)Ref',
    `source_parameter_ref` varchar(45) NOT NULL COMMENT '源参数Ref',
    `target_task_ref`      varchar(45) NOT NULL COMMENT '目标任务(引用的任务)Ref',
    `target_parameter_ref` varchar(45) NOT NULL COMMENT '目标参数Ref'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='参数关联表';

CREATE TABLE `task_instance`
(
    `id`                varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `serial_no`         int                                    DEFAULT NULL COMMENT '执行序号',
    `def_key`           varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '任务定义唯一Key',
    `async_task_ref`    varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '流程定义上下文中的AsyncTask唯一标识',
    `workflow_ref`      varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义Ref',
    `workflow_version`  varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义版本',
    `business_id`       varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '外部业务ID',
    `project_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目ID',
    `start_time`        datetime                               DEFAULT NULL COMMENT '开始时间',
    `end_time`          datetime                               DEFAULT NULL COMMENT '结束时间',
    `result_file`       longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '执行结果文件',
    `status`            varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '任务运行状态',
    `output_parameters` blob COMMENT '任务实例输出参数',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务实例表';

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
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务实例参数表';

CREATE TABLE `parameter`
(
    `id`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数ID',
    `type`  varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型',
    `value` blob                                                         NOT NULL COMMENT '参数值',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='参数存储表';

CREATE TABLE `worker`
(
    `id`     varchar(45) NOT NULL COMMENT 'ID',
    `name`   varchar(45) DEFAULT NULL COMMENT '名称',
    `status` varchar(45) DEFAULT NULL COMMENT '状态',
    `type`   varchar(45) DEFAULT NULL COMMENT '类型',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务执行器';

CREATE TABLE `secret_namespace`
(
    `name`               varchar(100) NOT NULL COMMENT '名称',
    `description`        varchar(255) DEFAULT NULL COMMENT '描述',
    `created_time`       datetime     NOT NULL COMMENT '创建时间',
    `last_modified_time` datetime     NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='密钥命名空间表';

CREATE TABLE `secret_kv_pair`
(
    `namespace_name` varchar(100) NOT NULL COMMENT '命名空间名称',
    `kv_key`         varchar(45)  NOT NULL COMMENT '参数key',
    `kv_value`       text         NOT NULL COMMENT '参数值'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='密钥键值对表';