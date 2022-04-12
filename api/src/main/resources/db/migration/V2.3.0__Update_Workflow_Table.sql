ALTER TABLE `workflow`
    add `created_time` datetime DEFAULT NULL COMMENT '创建时间';

ALTER TABLE `workflow_instance`
    add `suspended_time` datetime DEFAULT NULL COMMENT '挂起时间' AFTER `start_time`;

ALTER TABLE `async_task_instance`
    add `failure_mode` varchar(45) DEFAULT 'SUSPEND' COMMENT '错误处理模式' AFTER `status`;
