ALTER TABLE `hub_node_definition_version`
    add `description` varchar(200) DEFAULT NULL COMMENT '描述' AFTER `version`;