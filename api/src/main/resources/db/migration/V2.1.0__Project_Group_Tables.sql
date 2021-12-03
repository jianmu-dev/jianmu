CREATE TABLE `project_group`
(
    `id`                 varchar(45) NOT NULL COMMENT 'ID',
    `name`               varchar(45) NOT NULL COMMENT '名称',
    `description`        varchar(255) DEFAULT NULL COMMENT '描述',
    `sort`               int         NOT NULL COMMENT '排序',
    `project_count`      int         NOT NULL COMMENT '项目数量',
    `created_time`       datetime    NOT NULL COMMENT '创建时间',
    `last_modified_time` datetime    NOT NULL COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `name_UNIQUE` (`name`),
    UNIQUE KEY `sort_UNIQUE` (`sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='项目组表';

INSERT INTO `project_group`(`id`, `name`, `description`, `sort`, `project_count`, `created_time`, `last_modified_time`)
VALUES ('1', '默认分组', '默认分组', 0, 0, now(), now());

CREATE TABLE `project_link_group`
(
    `id`               varchar(45) NOT NULL COMMENT 'ID',
    `project_id`       varchar(45) NOT NULL COMMENT '项目ID',
    `project_group_id` varchar(45) NOT NULL COMMENT '项目组ID',
    `sort`             int         NOT NULL COMMENT '排序',
    `created_time`     datetime    NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `project_UNIQUE` (`project_id`),
    UNIQUE KEY `project_group_sort_UNIQUE` (`project_group_id`, `sort`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='项目-项目组中间表';

DELIMITER //
CREATE PROCEDURE `add_project_link_group`()
BEGIN
    DECLARE `i` int DEFAULT (0);
    DECLARE `sort` int DEFAULT (0);
    DECLARE `project_id` VARCHAR(50);
    DECLARE time VARCHAR(20);
    DECLARE `project_cursor` CURSOR FOR SELECT `id`, `created_time` FROM `jianmu_project`;
    DECLARE EXIT HANDLER FOR NOT FOUND SET `i` := 1;
    OPEN `project_cursor`;
    WHILE `i` = 0
        DO
            FETCH `project_cursor` INTO `project_id`, `time`;
            INSERT INTO `project_link_group` VALUES (REPLACE(UUID(), '_', ''), `project_id`, '1', `sort`, `time`);
            UPDATE `project_group` SET `project_count`=sort + 1 where `id` = 1;
            SET `sort` := `sort` + 1;
        END WHILE;
    CLOSE `project_cursor`;
END //
DELIMITER ;

CALL `add_project_link_group`;
DROP PROCEDURE `add_project_link_group`;