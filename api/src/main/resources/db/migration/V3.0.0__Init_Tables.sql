CREATE TABLE `jm_project`
(
    `id`                   varchar(45)  NOT NULL COMMENT 'ID',
    `dsl_source`           varchar(45)           DEFAULT NULL COMMENT 'DSL来源',
    `dsl_type`             varchar(45)           DEFAULT NULL COMMENT 'DSL 类型',
    `trigger_type`         varchar(45)           DEFAULT NULL COMMENT '触发类型',
    `git_repo_id`          varchar(150) NOT NULL COMMENT 'Git仓库ID',
    `workflow_name`        varchar(45)  NOT NULL COMMENT '流程定义显示名称',
    `workflow_description` varchar(255)          DEFAULT NULL COMMENT '描述',
    `workflow_ref`         varchar(45)  NOT NULL COMMENT '流程定义Ref',
    `workflow_version`     varchar(45)  NOT NULL COMMENT '流程定义版本',
    `steps`                int          NOT NULL COMMENT '步骤数量',
    `enabled`              tinyint(1)            DEFAULT NULL COMMENT '项目是否可触发',
    `mutable`              tinyint(1)            DEFAULT NULL COMMENT '项目状态是否可变',
    `concurrent`           bit                   DEFAULT 0 COMMENT '可否并发执行',
    `dsl_text`             longtext     NOT NULL COMMENT 'DSL内容文本',
    `created_time`         datetime              DEFAULT NULL COMMENT '创建时间',
    `last_modified_by`     varchar(45)           DEFAULT NULL COMMENT '最后修改人',
    `last_modified_time`   datetime     NOT NULL COMMENT '最后修改时间',
    `association_id`       varchar(45)  NOT NULL DEFAULT '' COMMENT '关联ID',
    `association_type`     varchar(16)  NOT NULL DEFAULT '' COMMENT '关联类型',
    PRIMARY KEY (`id`),
    UNIQUE KEY `workflow_ref_UNIQUE` (`workflow_ref`),
    UNIQUE INDEX `id_type_workflow_name_UNIQUE` (`association_id`, `association_type`, `workflow_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='DSL表';

CREATE TABLE `jm_trigger`
(
    `id`         varchar(45)  NOT NULL COMMENT 'ID',
    `ref`        varchar(512) NOT NULL COMMENT 'ref',
    `project_id` varchar(45)  NOT NULL COMMENT '项目ID',
    `type`       varchar(45)  NOT NULL COMMENT '触发器类型',
    `schedule`   varchar(45) DEFAULT NULL COMMENT 'Cron表达式',
    `webhook`    blob COMMENT 'webhook对象',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `ref` (`ref`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='建木触发器表';

CREATE TABLE `jm_web_request`
(
    `id`               varchar(45) NOT NULL COMMENT 'ID',
    `project_id`       varchar(45) DEFAULT NULL COMMENT '项目ID',
    `workflow_ref`     varchar(45) DEFAULT NULL COMMENT '流程Ref',
    `workflow_version` varchar(45) DEFAULT NULL COMMENT '流程版本',
    `trigger_id`       varchar(45) DEFAULT NULL COMMENT '触发器ID',
    `user_agent`       text COMMENT 'UserAgent',
    `payload`          text COMMENT '请求载荷',
    `status_code`      varchar(45) NOT NULL COMMENT '状态枚举',
    `error_msg`        text COMMENT '错误信息',
    `request_time`     datetime    NOT NULL COMMENT '请求时间',
    PRIMARY KEY (`id`),
    INDEX request_time (`request_time` desc)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Web请求表';

CREATE TABLE `jm_trigger_event`
(
    `id`             varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件ID',
    `project_id`     varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目ID',
    `trigger_id`     varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器ID',
    `web_request_id` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'WebRequest ID',
    `trigger_type`   varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器类型',
    `payload`        text COLLATE utf8mb4_unicode_ci COMMENT '事件载荷',
    `occurred_time`  datetime                               NOT NULL COMMENT '触发时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='触发器事件表';

CREATE TABLE `jm_trigger_event_parameter`
(
    `trigger_event_id` varchar(45) NOT NULL COMMENT '触发器事件ID',
    `ref`              varchar(45) NOT NULL COMMENT '唯一引用名称',
    `name`             varchar(45) NOT NULL COMMENT '参数名',
    `type`             varchar(45) NOT NULL COMMENT '参数类型',
    `value`            text        NOT NULL COMMENT '参数值',
    `required`         bit         NOT NULL COMMENT '是否必填',
    `hidden`           bit         NOT NULL DEFAULT 0 COMMENT '是否隐藏',
    `parameter_id`     varchar(45) NOT NULL COMMENT '参数引用ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='事件参数表';

CREATE TABLE `jm_workflow`
(
    `ref_version`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程定义标识，主键',
    `ref`               varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '唯一引用名称',
    `version`           varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '版本',
    `type`              varchar(45) COLLATE utf8mb4_unicode_ci                        NOT NULL COMMENT 'DSL 类型',
    `name`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
    `tag`               varchar(255)                                                  NOT NULL COMMENT '标签',
    `description`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `nodes`             longblob COMMENT 'Node列表',
    `global_parameters` blob COMMENT '全局参数',
    `dsl_text`          longtext COLLATE utf8mb4_unicode_ci                           NOT NULL COMMENT 'DSL内容',
    `created_time`      datetime                                                      DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`ref_version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='流程定义表';

CREATE TABLE `jm_task_instance`
(
    `id`               varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '主键',
    `serial_no`        int                                                          DEFAULT NULL COMMENT '执行序号',
    `def_key`          varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '任务定义唯一Key',
    `node_info`        blob                                                          NOT NULL COMMENT '节点定义快照',
    `async_task_ref`   varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '流程定义上下文中的AsyncTask唯一标识',
    `workflow_ref`     varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义Ref',
    `workflow_version` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程定义版本',
    `business_id`      varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '外部业务ID',
    `trigger_id`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Trigger ID',
    `worker_id`        varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Worker ID',
    `start_time`       datetime                                                     DEFAULT NULL COMMENT '开始时间',
    `end_time`         datetime                                                     DEFAULT NULL COMMENT '结束时间',
    `status`           varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '任务运行状态',
    `_version`         int                                                           NOT NULL COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='任务实例表';

CREATE TABLE `jm_task_instance_parameter`
(
    `instance_id`    varchar(45)  NOT NULL COMMENT '任务实例ID',
    `serial_no`      int          NOT NULL COMMENT '执行序号',
    `def_key`        varchar(45)  NOT NULL COMMENT '任务定义Key（类型）',
    `async_task_ref` varchar(45)  NOT NULL COMMENT '任务节点ref',
    `business_id`    varchar(45)  NOT NULL COMMENT '流程实例ID',
    `trigger_id`     varchar(255) NOT NULL COMMENT '外部触发ID，流程实例唯一',
    `ref`            varchar(45)  NOT NULL COMMENT '参数ref',
    `type`           varchar(45)  NOT NULL COMMENT '参数类型',
    `workflow_type`  varchar(45)  NOT NULL COMMENT '流程类型',
    `parameter_id`   varchar(45)  NOT NULL COMMENT '参数引用ID',
    `required`       bit(1)       NOT NULL COMMENT '是否必填'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务实例参数表';

CREATE TABLE `jm_workflow_instance`
(
    `id`                varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '唯一ID主键',
    `serial_no`         int                                                           NOT NULL COMMENT '执行顺序',
    `trigger_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器ID',
    `trigger_type`      varchar(45) COLLATE utf8mb4_unicode_ci                        DEFAULT NULL COMMENT 'Trigger Type',
    `name`              varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '显示名称',
    `description`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `run_mode`          varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '运行模式',
    `status`            varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '运行状态',
    `workflow_ref`      varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '流程定义唯一引用名称',
    `workflow_version`  varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '流程定义版本',
    `global_parameters` blob COMMENT '全局参数列表',
    `start_time`        datetime                                                      DEFAULT NULL COMMENT '开始时间',
    `suspended_time`    datetime                                                      DEFAULT NULL COMMENT '挂起时间',
    `end_time`          datetime                                                      DEFAULT NULL COMMENT '结束时间',
    `_version`          int                                                           NOT NULL COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`),
    UNIQUE INDEX trigger_id (`trigger_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='流程实例表';

CREATE TABLE `jm_parameter`
(
    `id`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数ID',
    `type`  varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数类型',
    `value` blob                                                         NOT NULL COMMENT '参数值',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='参数存储表';

CREATE TABLE `jm_worker`
(
    `id`           varchar(45) NOT NULL COMMENT 'ID',
    `name`         varchar(45)  DEFAULT NULL COMMENT '名称',
    `status`       varchar(45)  DEFAULT NULL COMMENT '状态',
    `type`         varchar(45)  DEFAULT NULL COMMENT '类型',
    `tags`         varchar(100) DEFAULT NULL COMMENT 'Worker ID',
    `capacity`     int          DEFAULT NULL COMMENT '容量',
    `os`           varchar(45)  DEFAULT NULL COMMENT 'os',
    `arch`         varchar(45)  DEFAULT NULL COMMENT '架构',
    `created_time` datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='任务执行器';

CREATE TABLE `jm_secret_namespace`
(
    `association_id`     varchar(45)  NOT NULL DEFAULT '' COMMENT '关联ID',
    `association_type`   varchar(16)  NOT NULL DEFAULT '' COMMENT '关联类型',
    `name`               varchar(100) NOT NULL COMMENT '名称',
    `description`        varchar(255)          DEFAULT NULL COMMENT '描述',
    `created_time`       datetime     NOT NULL COMMENT '创建时间',
    `last_modified_time` datetime     NOT NULL COMMENT '修改时间',
    UNIQUE INDEX id_type_name (`association_id`, `association_type`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='密钥命名空间表';

CREATE TABLE `jm_secret_kv_pair`
(
    `association_id`   varchar(45)  NOT NULL DEFAULT '' COMMENT '关联ID',
    `association_type` varchar(16)  NOT NULL DEFAULT '' COMMENT '关联类型',
    `namespace_name`   varchar(100) NOT NULL COMMENT '命名空间名称',
    `kv_key`           varchar(45)  NOT NULL COMMENT '参数key',
    `kv_value`         text         NOT NULL COMMENT '参数值',
    UNIQUE INDEX id_type_name_key (`association_id`, `association_type`, `namespace_name`, `kv_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='密钥键值对表';

CREATE TABLE `jm_hub_node_definition`
(
    `id`            varchar(45) NOT NULL COMMENT 'ID',
    `icon`          varchar(255) DEFAULT NULL COMMENT '图标地址',
    `name`          varchar(45)  DEFAULT NULL COMMENT '名称',
    `owner_name`    varchar(45)  DEFAULT NULL COMMENT '所有者名称',
    `owner_type`    varchar(45)  DEFAULT NULL COMMENT '所有者类型',
    `owner_ref`     varchar(45)  DEFAULT NULL COMMENT '所有者唯一引用',
    `creator_name`  varchar(45)  DEFAULT NULL COMMENT '创建者名称',
    `creator_ref`   varchar(45)  DEFAULT NULL COMMENT '创建者唯一引用',
    `type`          varchar(45)  DEFAULT NULL COMMENT '节点类型',
    `description`   tinytext COMMENT '描述',
    `ref`           varchar(45)  DEFAULT NULL COMMENT '唯一引用',
    `source_link`   varchar(255) DEFAULT NULL COMMENT '源码链接',
    `document_link` varchar(255) DEFAULT NULL COMMENT '下载链接',
    `deprecated`    bit          DEFAULT 0 COMMENT '弃用',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='节点定义表';

CREATE TABLE `jm_hub_node_definition_version`
(
    `id`                varchar(45) NOT NULL COMMENT 'ID',
    `owner_ref`         varchar(45)  DEFAULT NULL COMMENT '所有者唯一引用',
    `ref`               varchar(45)  DEFAULT NULL COMMENT '唯一引用',
    `creator_name`      varchar(45)  DEFAULT NULL COMMENT '创建者名称',
    `creator_ref`       varchar(45)  DEFAULT NULL COMMENT '创建者唯一引用',
    `version`           varchar(45)  DEFAULT NULL COMMENT '版本',
    `description`       varchar(200) DEFAULT NULL COMMENT '描述',
    `result_file`       varchar(45)  DEFAULT NULL COMMENT '结果文件',
    `type`              varchar(45)  DEFAULT NULL COMMENT '类型',
    `input_parameters`  blob COMMENT '输入参数列表',
    `output_parameters` blob COMMENT '输出参数列表',
    `spec`              longtext COMMENT '规格',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='节点定义版本表';

CREATE TABLE `jm_shell_node_def`
(
    `id`         varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID',
    `shell_node` text COLLATE utf8mb4_unicode_ci         NOT NULL COMMENT '序列化对象',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Shell节点定义表';

CREATE TABLE `jm_git_repo`
(
    `id`       varchar(45)  NOT NULL COMMENT 'ID',
    `ref`      varchar(128) NOT NULL DEFAULT '' COMMENT '唯一表示',
    `owner`    varchar(128) NOT NULL DEFAULT '' COMMENT '拥有者',
    `branches` blob         NOT NULL COMMENT '分支',
    `flows`    blob COMMENT '流水线',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='git仓库';

CREATE TABLE `jm_project_group`
(
    `id`                 varchar(45) NOT NULL COMMENT 'ID',
    `name`               varchar(45) NOT NULL COMMENT '名称',
    `description`        varchar(256) DEFAULT NULL COMMENT '描述',
    `sort`               int         NOT NULL COMMENT '排序',
    `is_show`            bit         NOT NULL COMMENT '是否展示',
    `project_count`      int         NOT NULL COMMENT '项目数量',
    `created_time`       datetime    NOT NULL COMMENT '创建时间',
    `last_modified_time` datetime    NOT NULL COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`),
    UNIQUE KEY `sort_UNIQUE` (`sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='项目组表';

INSERT INTO `jm_project_group`(`id`, `name`, `description`, `sort`, `is_show`, `project_count`, `created_time`,
                               `last_modified_time`)
VALUES ('1', '默认分组', '默认分组', 0, 1, 0, now(), now());

CREATE TABLE `jm_project_link_group`
(
    `id`               varchar(45) NOT NULL COMMENT 'ID',
    `project_id`       varchar(45) NOT NULL COMMENT '项目ID',
    `project_group_id` varchar(45) NOT NULL COMMENT '项目组ID',
    `sort`             int         NOT NULL COMMENT '排序',
    `created_time`     datetime    NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `project_UNIQUE` (`project_id`),
    UNIQUE KEY `project_group_sort_UNIQUE` (`project_group_id`, `sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='项目-项目组中间表';

CREATE TABLE IF NOT EXISTS `jm_async_task_instance`
(
    `id`                   varchar(45)  NOT NULL,
    `trigger_id`           varchar(45)  NOT NULL COMMENT 'Trigger ID',
    `workflow_ref`         varchar(45)  NOT NULL COMMENT '流程Ref',
    `workflow_version`     varchar(45)  NOT NULL COMMENT '流程版本',
    `workflow_instance_id` varchar(45)  NOT NULL COMMENT '流程实例ID',
    `name`                 varchar(45)  NOT NULL COMMENT '名称',
    `description`          varchar(255) NOT NULL COMMENT '描述',
    `status`               varchar(45)  NOT NULL COMMENT '状态',
    `failure_mode`         varchar(45) DEFAULT 'SUSPEND' COMMENT '错误处理模式',
    `async_task_ref`       varchar(45)  NOT NULL COMMENT '任务定义Ref',
    `async_task_type`      varchar(45)  NOT NULL COMMENT '任务定义类型',
    `serial_no`            integer     DEFAULT 0 COMMENT '完成次数累计',
    `next_target`          varchar(45) DEFAULT NULL COMMENT '下一个要触发的节点',
    `activating_time`      datetime     NOT NULL COMMENT '激活时间',
    `start_time`           datetime    DEFAULT NULL COMMENT '开始时间',
    `end_time`             datetime    DEFAULT NULL COMMENT '结束时间',
    `_version`             integer     DEFAULT 0 COMMENT '乐观锁版本字段',
    PRIMARY KEY (`id`),
    UNIQUE INDEX trigger_id_and_task_ref (`trigger_id`, `async_task_ref`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='异步任务实例表';

CREATE TABLE `jm_user`
(
    `id`         varchar(128) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT 'ID',
    `avatar_url` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像地址',
    `nickname`   varchar(128) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '昵称',
    `data`       text COLLATE utf8mb4_unicode_ci          NOT NULL COMMENT '用户数据',
    `username`   varchar(128) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '用户名',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';


CREATE TABLE `jm_external_parameter`
(
    `id`                 varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID',
    `ref`                varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数唯一标识',
    `name`               varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名',
    `label`              varchar(32) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '参数标签',
    `type`               varchar(64) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '参数类型',
    `value`              varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数值',
    `created_time`       datetime                                         DEFAULT NULL COMMENT '创建时间',
    `last_modified_time` datetime                                NOT NULL COMMENT '最后修改时间',
    `association_id`     varchar(45)                             NOT NULL DEFAULT '' COMMENT '关联ID',
    `association_type`   varchar(16)                             NOT NULL DEFAULT '' COMMENT '关联类型',
    UNIQUE INDEX association_id_type_ref (`association_id`, `association_type`, `ref`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='外部参数表';

CREATE TABLE `jm_external_parameter_label`
(
    `id`                 varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID',
    `value`              varchar(32) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '参数标签值',
    `created_time`       datetime                                         DEFAULT NULL COMMENT '创建时间',
    `last_modified_time` datetime                                NOT NULL COMMENT '最后修改时间',
    `association_id`     varchar(45)                             NOT NULL DEFAULT '' COMMENT '关联ID',
    `association_type`   varchar(16)                             NOT NULL DEFAULT '' COMMENT '关联类型',
    UNIQUE INDEX association_id_type_value (`association_id`, `association_type`, `value`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='外部参数标签表';

CREATE TABLE `jm_project_last_execution`
(
    `workflow_ref`   varchar(45) NOT NULL COMMENT 'ID',
    `status`         varchar(45) DEFAULT NULL COMMENT '运行状态',
    `start_time`     datetime    DEFAULT NULL COMMENT '开始时间',
    `suspended_time` datetime    DEFAULT NULL COMMENT '挂起时间',
    `end_time`       datetime    DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`workflow_ref`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='项目最后执行记录';