ALTER TABLE `jm_task_instance`
    ADD INDEX `idx_businessid` (`business_id`);
ALTER TABLE `jm_task_instance`
    ADD INDEX `idx_triggerid` (`trigger_id`);
ALTER TABLE `jm_task_instance`
    ADD INDEX `idx_workerid` (`worker_id`);
ALTER TABLE `jm_async_task_instance`
    ADD INDEX `idx_workflowinstanceid` (`workflow_instance_id`);
ALTER TABLE `jm_workflow_instance`
    ADD INDEX `idx_workflowref` (`workflow_ref`);
