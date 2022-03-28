ALTER TABLE `jianmu_project`
    add `concurrent` bit DEFAULT 0 COMMENT '可否并发执行';

ALTER TABLE `async_task_instance`
    add `serial_no` integer DEFAULT 0 COMMENT '完成次数累计';