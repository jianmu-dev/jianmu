ALTER TABLE `jm_workflow`
    ADD COLUMN `caches` blob COMMENT '缓存' AFTER `tag`;

ALTER TABLE `jm_volume`
    ADD COLUMN `workflow_ref` varchar(45) DEFAULT NULL COMMENT '流程唯一标识' AFTER `worker_id`;
ALTER TABLE `jm_volume`
    ADD COLUMN `cleaning` bit(1) NOT NULL COMMENT '是否正在清理' AFTER `taint`;