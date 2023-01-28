ALTER TABLE `jm_trigger`
    MODIFY `ref` varchar(512) NULL COMMENT '唯一标识';

UPDATE `jm_trigger` SET `ref` = null where `type` = 'CRON';
UPDATE `jm_trigger` SET `webhook` = null where `type` = 'CRON';
UPDATE `jm_trigger` SET `schedule` = null where `type` = 'WEBHOOK';