ALTER TABLE `jm_project`
    ADD COLUMN `creator_id` varchar(64) DEFAULT NULL COMMENT '创建者ID' AFTER `dsl_text`;
ALTER TABLE `jm_project`
    ADD COLUMN `last_modified_by_id` varchar(64) DEFAULT NULL COMMENT '最后修改者ID' AFTER `last_modified_by`;