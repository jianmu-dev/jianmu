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
    PRIMARY KEY (`id`)
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
);

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
);

CREATE TABLE `worker`
(
    `id`     varchar(45) NOT NULL COMMENT 'ID',
    `name`   varchar(45) DEFAULT NULL COMMENT '名称',
    `status` varchar(45) DEFAULT NULL COMMENT '状态',
    `type`   varchar(45) DEFAULT NULL COMMENT '类型',
    PRIMARY KEY (`id`)
);

insert into worker(id, name, status, type) values ('worker9527', 'Worker1', 'OFFLINE', 'DOCKER');

insert into workflow_instance(id, name, description, run_mode, status, workflow_ref, workflow_version, task_instances,
                              start_time, end_time, _version)
values ('6e8840f303c949b09f3b50cb7ce88bad', 'TestWL', '测试流程1', 'AUTO', 'RUNNING', 'test_wl1', '1.0', convert('[
    "java.util.ImmutableCollections$List12",
    [
        [
            "dev.jianmu.workflow.aggregate.process.AsyncTaskInstance",
            {
                "name": "AsyncTask1",
                "description": "测试流程1",
                "status": "INIT",
                "asyncTaskRef": "asyncTask_1",
                "startTime": "2021-03-22T17:03:35.020903",
                "endTime": null
            }
        ]
    ]
]', BINARY), '2021-03-22 17:03:35', NULL, '2');

insert into workflow(ref_version, ref, version, name, description, nodes)
values ('test_wl11.0', 'test_wl1', '1.0', 'TestWL', '测试流程1', convert('[
    "java.util.ImmutableCollections$SetN",
    [
        [
            "dev.jianmu.workflow.aggregate.definition.AsyncTask",
            {
                "name": "AsyncTask3",
                "ref": "asyncTask_3",
                "description": "异步任务节点3",
                "sources": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "asyncTask_1"
                    ]
                ],
                "targets": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "end_1"
                    ]
                ],
                "type": "AsyncTask"
            }
        ],
        [
            "dev.jianmu.workflow.aggregate.definition.Start",
            {
                "name": "Start1",
                "ref": "start_1",
                "description": "开始节点1",
                "sources": [
                    "java.util.ImmutableCollections$SetN",
                    []
                ],
                "targets": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "condition_1"
                    ]
                ],
                "type": "Start"
            }
        ],
        [
            "dev.jianmu.workflow.aggregate.definition.AsyncTask",
            {
                "name": "AsyncTask2",
                "ref": "asyncTask_2",
                "description": "异步任务节点2",
                "sources": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "condition_1"
                    ]
                ],
                "targets": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "end_1"
                    ]
                ],
                "type": "AsyncTask"
            }
        ],
        [
            "dev.jianmu.workflow.aggregate.definition.AsyncTask",
            {
                "name": "AsyncTask1",
                "ref": "asyncTask_1",
                "description": "异步任务节点1",
                "sources": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "condition_1"
                    ]
                ],
                "targets": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "end_1"
                    ]
                ],
                "type": "AsyncTask"
            }
        ],
        [
            "dev.jianmu.workflow.aggregate.definition.Condition",
            {
                "name": "Condition1",
                "ref": "condition_1",
                "description": "条件网关1",
                "sources": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "start_1"
                    ]
                ],
                "targets": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "asyncTask_2",
                        "asyncTask_1"
                    ]
                ],
                "type": "Condition",
                "targetMap": [
                    "java.util.HashMap",
                    {
                        "false": "asyncTask_2",
                        "true": "asyncTask_1"
                    }
                ],
                "expression": "1+1==2"
            }
        ],
        [
            "dev.jianmu.workflow.aggregate.definition.End",
            {
                "name": "End1",
                "ref": "end_1",
                "description": "结束节点1",
                "sources": [
                    "java.util.ImmutableCollections$Set12",
                    [
                        "asyncTask_2",
                        "asyncTask_3"
                    ]
                ],
                "targets": [
                    "java.util.ImmutableCollections$SetN",
                    []
                ],
                "type": "End"
            }
        ]
    ]
]', BINARY))