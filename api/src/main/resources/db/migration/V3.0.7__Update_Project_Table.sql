ALTER TABLE `jianmu_project`
    MODIFY `concurrent` INT NOT NULL COMMENT '并发执行数';
UPDATE `jianmu_project` set `concurrent` = 9 where `concurrent` = 1;
UPDATE `jianmu_project` set `concurrent` = 1 where `concurrent` = 0;
