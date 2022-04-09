ALTER TABLE `jianmu_project`
    add `concurrent` bit DEFAULT 0 COMMENT '可否并发执行';

ALTER TABLE `async_task_instance`
    add `serial_no` integer DEFAULT 0 COMMENT '完成次数累计';

ALTER TABLE `async_task_instance`
    add `next_target` varchar(45) DEFAULT NULL COMMENT '下一个要触发的节点';

ALTER TABLE `async_task_instance`
    add `_version` integer DEFAULT 0 COMMENT '乐观锁版本字段';