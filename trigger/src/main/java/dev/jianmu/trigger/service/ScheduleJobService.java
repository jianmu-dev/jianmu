package dev.jianmu.trigger.service;

import dev.jianmu.trigger.entity.TriggerEntity;
import dev.jianmu.trigger.repository.TriggerRepository;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @class: ScheduleJobService
 * @description: ScheduleJobService
 * @author: Ethan Liu
 * @create: 2021-05-24 10:30
 **/
@Service
public class ScheduleJobService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleJobService.class);
    private final TriggerRepository triggerRepository;
    private final Scheduler scheduler;

    public ScheduleJobService(TriggerRepository triggerRepository, Scheduler scheduler) {
        this.triggerRepository = triggerRepository;
        this.scheduler = scheduler;
    }

    @Transactional
    public void addTrigger(String projectId, String cron) {
        var entity = TriggerEntity.Builder.aTriggerEntity()
                .projectId(projectId)
                .cron(cron)
                .build();
        var jobDetail = this.createJobDetail(entity);
        var trigger = this.createCronTrigger(entity);
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            logger.error("触发器加载失败: {}", e.getMessage());
            throw new RuntimeException("触发器加载失败");
        }
        this.triggerRepository.add(entity);
    }

    @Transactional
    public void deleteTrigger(String triggerId) {
        try {
            // 停止触发器
            scheduler.pauseTrigger(TriggerKey.triggerKey(triggerId));
            // 卸载任务
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggerId));
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(triggerId));
        } catch (SchedulerException e) {
            logger.error("触发器删除失败: {}", e.getMessage());
            throw new RuntimeException("触发器删除失败");
        }
        this.triggerRepository.deleteByTriggerId(triggerId);
    }

    public void startTriggers() {
        var triggers = this.triggerRepository.findAll();
        if (triggers.isEmpty()) {
            logger.info("没有可加载的触发器");
            return;
        }
        triggers.forEach(triggerEntity -> {
            var cronTrigger = this.createCronTrigger(triggerEntity);
            var jobDetail = this.createJobDetail(triggerEntity);
            try {
                scheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (SchedulerException e) {
                logger.error("触发器加载失败: {}", e.getMessage());
                throw new RuntimeException("触发器加载失败");
            }
        });
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("触发器启动失败: {}", e.getMessage());
            throw new RuntimeException("触发器启动失败");
        }
    }

    private CronTrigger createCronTrigger(TriggerEntity triggerEntity) {
        var builder = CronScheduleBuilder.cronSchedule(triggerEntity.getCron());
        return TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey(triggerEntity.getProjectId()))
                .withSchedule(builder)
                .build();
    }

    private JobDetail createJobDetail(TriggerEntity triggerEntity) {
        return JobBuilder.newJob()
                .withIdentity(JobKey.jobKey(triggerEntity.getProjectId()))
                .build();
    }
}
