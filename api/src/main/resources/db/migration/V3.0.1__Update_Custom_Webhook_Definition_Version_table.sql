ALTER TABLE `jm_custom_webhook_definition_version`
    ADD COLUMN `created_time` datetime DEFAULT now() COMMENT '创建时间' AFTER `dsl_text`;
ALTER TABLE `jm_custom_webhook_definition_version`
    ADD COLUMN `last_modified_time` datetime DEFAULT now() COMMENT '最后修改时间' AFTER `created_time`;
