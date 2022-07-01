CREATE TABLE `user`
(
    `id`       varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ID',
    `head_url`  varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '头像地址',
    `nickname` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
    `data` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户数据',
    `username` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';