ALTER TABLE `workflow_instance`
    add UNIQUE INDEX trigger_id(`trigger_id`);
ALTER TABLE `workflow`
    add tag varchar(45)  comment '标签';