ALTER TABLE `jm_project`
    ADD COLUMN `creator_id` varchar(64) NOT NULL COMMENT '创建者ID' AFTER `dsl_text`;