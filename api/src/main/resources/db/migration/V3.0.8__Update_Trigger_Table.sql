ALTER TABLE `jm_trigger`
    MODIFY `ref` varchar(512) NULL COMMENT '唯一标识';

UPDATE `jm_trigger` SET `ref` = null where `type` = 'CRON';