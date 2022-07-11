DROP TABLE `git_repo`;

CREATE TABLE `git_repo`
(
    `id`          varchar(45) NOT NULL COMMENT 'ID',
    `branches`    blob        NOT NULL COMMENT '分支',
    `flows`       blob                 COMMENT '流水线',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='git仓库';