insert into worker(id, name, status, type)
values ('worker9527', 'Worker1', 'OFFLINE', 'DOCKER');

insert into workflow_instance(id, trigger_id, name, description, run_mode, status, workflow_ref, workflow_version, task_instances,
                              start_time, end_time, _version)
values ('6e8840f303c949b09f3b50cb7ce88bad','98dae6f56f8344d2be498cb3f96e9f2a', 'TestWL', '测试流程1', 'AUTO', 'RUNNING', 'test_wl1', '1.0', convert('[
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
]', BINARY));