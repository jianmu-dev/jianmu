CREATE TABLE `volume`
(
    `id`             varchar(45) NOT NULL COMMENT 'ID',
    `name`           varchar(45) DEFAULT NULL COMMENT 'Volume名称',
    `scope`          varchar(45) DEFAULT NULL COMMENT '使用范围',
    `worker_id`      varchar(45) DEFAULT NULL COMMENT 'Worker ID',
    `available`      bit(1)      NOT NULL COMMENT '是否可用',
    `taint`          bit(1)      NOT NULL COMMENT '是否被污染，无法删除',
    `created_time`   datetime    NOT NULL COMMENT '创建时间',
    `available_time` datetime    DEFAULT NULL COMMENT '可用时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Volume表';