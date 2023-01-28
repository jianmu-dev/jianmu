UPDATE `jianmu_trigger` SET `webhook` = null where `type` = 'CRON';
UPDATE `jianmu_trigger` SET `schedule` = null where `type` = 'WEBHOOK';