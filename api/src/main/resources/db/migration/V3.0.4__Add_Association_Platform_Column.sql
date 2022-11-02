# jm_project
ALTER TABLE `jm_project`
    ADD `association_platform` varchar(32) NOT NULL DEFAULT '' COMMENT '关联平台' AFTER `association_type`;
ALTER TABLE `jm_project`
    DROP INDEX `id_type_workflow_name_UNIQUE`;
ALTER TABLE `jm_project`
    ADD UNIQUE `association_id_type_platform_workflow_name` (`association_id`, `association_type`,
                                                             `association_platform`, `workflow_name`);
UPDATE `jm_project`
SET `association_platform` = 'GITLINK'
WHERE `association_id` != ''
  AND `association_type` != ''
  AND `association_platform` = '';

# jm_secret_namespace
ALTER TABLE `jm_secret_namespace`
    ADD `association_platform` varchar(32) NOT NULL DEFAULT '' COMMENT '关联平台' AFTER `association_type`;
ALTER TABLE `jm_secret_namespace`
    DROP INDEX `id_type_name`;
ALTER TABLE `jm_secret_namespace`
    ADD UNIQUE `association_id_type_platform_name` (`association_id`, `association_type`, `association_platform`, `name`);

UPDATE `jm_secret_namespace`
SET `association_platform` = 'GITLINK'
WHERE `association_id` != ''
  AND `association_type` != ''
  AND `association_platform` = '';

# jm_secret_kv_pair
ALTER TABLE `jm_secret_kv_pair`
    ADD `association_platform` varchar(32) NOT NULL DEFAULT '' COMMENT '关联平台' AFTER `association_type`;
ALTER TABLE `jm_secret_kv_pair`
    DROP INDEX `id_type_name_key`;
ALTER TABLE `jm_secret_kv_pair`
    ADD UNIQUE `association_id_type_platform_name_key` (`association_id`, `association_type`, `association_platform`,
                                                        `namespace_name`, `kv_key`);
UPDATE `jm_secret_kv_pair`
SET `association_platform` = 'GITLINK'
WHERE `association_id` != ''
  AND `association_type` != ''
  AND `association_platform` = '';

# jm_external_parameter
ALTER TABLE `jm_external_parameter`
    ADD `association_platform` varchar(32) NOT NULL DEFAULT '' COMMENT '关联平台' AFTER `association_type`;
ALTER TABLE `jm_external_parameter`
    DROP INDEX `association_id_type_ref`;
ALTER TABLE `jm_external_parameter`
    ADD UNIQUE `association_id_type_platform_ref` (`association_id`, `association_type`, `association_platform`, `ref`);

UPDATE `jm_external_parameter`
SET `association_platform` = 'GITLINK'
WHERE `association_id` != ''
  AND `association_type` != ''
  AND `association_platform` = '';

# jm_external_parameter_label
ALTER TABLE `jm_external_parameter_label`
    ADD `association_platform` varchar(32) NOT NULL DEFAULT '' COMMENT '关联平台' AFTER `association_type`;
ALTER TABLE `jm_external_parameter_label`
    DROP INDEX `association_id_type_value`;
ALTER TABLE `jm_external_parameter_label`
    ADD UNIQUE `association_id_type_platform_value` (`association_id`, `association_type`, `association_platform`, `value`);

UPDATE `jm_external_parameter_label`
SET `association_platform` = 'GITLINK'
WHERE `association_id` != ''
  AND `association_type` != ''
  AND `association_platform` = '';


