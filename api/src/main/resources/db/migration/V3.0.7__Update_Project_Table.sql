ALTER TABLE `jm_project`
    MODIFY `concurrent` INT NOT NULL COMMENT '并发执行数';
UPDATE `jm_project` set `concurrent` = 9 where `concurrent` = 1;
UPDATE `jm_project` set `concurrent` = 1 where `concurrent` = 0;
