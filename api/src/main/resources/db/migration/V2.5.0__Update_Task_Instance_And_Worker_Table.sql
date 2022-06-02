ALTER TABLE `task_instance`
    add `worker_id`      varchar(45) DEFAULT NULL COMMENT 'Worker ID';
ALTER TABLE `task_instance`
    add `_version`      int          NOT     NULL COMMENT '乐观锁版本字段' AFTER `worker_id`;

ALTER TABLE `worker`
    add `tags`          varchar(100) DEFAULT NULL COMMENT 'Worker ID'   AFTER `name`;
ALTER TABLE `worker`
    add `capacity`      int          DEFAULT NULL COMMENT '乐观锁版本字段' AFTER `tags`;
ALTER TABLE `worker`
    add `os`            varchar(45)  DEFAULT NULL COMMENT 'Worker ID'   AFTER `capacity`;
ALTER TABLE `worker`
    add `arch`          varchar(45)  DEFAULT NULL COMMENT 'Worker ID'   AFTER `os`;
ALTER TABLE `worker`
    add `created_time`  datetime     DEFAULT NULL COMMENT '创建时间'      AFTER `status`;