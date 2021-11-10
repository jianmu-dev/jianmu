package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.quartz.PublishJob;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.Webhook;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.repository.TriggerRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;

/**
 * @class: TriggerApplication
 * @description: TriggerApplication
 * @author: Ethan Liu
 * @create: 2021-11-10 11:15
 */
@Service
@Slf4j
public class TriggerApplication {
    private final TriggerRepository triggerRepository;
    private final Scheduler scheduler;
    private final ApplicationEventPublisher publisher;

    public TriggerApplication(TriggerRepository triggerRepository, Scheduler scheduler, ApplicationEventPublisher publisher) {
        this.triggerRepository = triggerRepository;
        this.scheduler = scheduler;
        this.publisher = publisher;
    }

    public void trigger(String triggerId) {
        var trigger = this.triggerRepository.findByTriggerId(triggerId)
                .orElseThrow(() -> new DataNotFoundException("未找到触发器"));
        var evt = TriggerEvent.Builder.aTriggerEvent()
                .projectId(trigger.getProjectId())
                .triggerId(trigger.getId())
                .type(trigger.getType().name())
                .build();
        this.publisher.publishEvent(evt);
    }

    @Transactional
    public void saveOrUpdate(String projectId, Webhook webhook) {
        var trigger = this.triggerRepository.findByProjectId(projectId)
                .orElse(
                        Trigger.Builder.aTrigger()
                                .projectId(projectId)
                                .type(Trigger.Type.WEBHOOK)
                                .webhook(webhook)
                                .build()
                );
    }

    @Transactional
    public void saveOrUpdate(String projectId, String schedule) {
        this.triggerRepository.findByProjectId(projectId)
                .ifPresentOrElse(trigger -> {
                    try {
                        // 更新schedule
                        trigger.setSchedule(schedule);
                        // 停止触发器
                        scheduler.pauseTrigger(TriggerKey.triggerKey(trigger.getId()));
                        // 卸载任务
                        scheduler.unscheduleJob(TriggerKey.triggerKey(trigger.getId()));
                        // 删除任务
                        scheduler.deleteJob(JobKey.jobKey(trigger.getId()));
                        var jobDetail = this.createJobDetail(trigger);
                        var cronTrigger = this.createCronTrigger(trigger);
                        scheduler.scheduleJob(jobDetail, cronTrigger);
                    } catch (SchedulerException e) {
                        log.error("触发器更新失败: {}", e.getMessage());
                        throw new RuntimeException("触发器更新失败");
                    }
                    this.triggerRepository.updateById(trigger);
                }, () -> {
                    var trigger = Trigger.Builder.aTrigger()
                            .projectId(projectId)
                            .type(Trigger.Type.CRON)
                            .schedule(schedule)
                            .build();
                    try {
                        var jobDetail = this.createJobDetail(trigger);
                        var cronTrigger = this.createCronTrigger(trigger);
                        scheduler.scheduleJob(jobDetail, cronTrigger);
                    } catch (SchedulerException e) {
                        log.error("触发器加载失败: {}", e.getMessage());
                        throw new RuntimeException("触发器加载失败");
                    }
                    this.triggerRepository.add(trigger);
                });
    }

    @Transactional
    public void deleteByProjectId(String projectId) {
        this.triggerRepository.findByProjectId(projectId)
                .ifPresent(trigger -> {
                    try {
                        // 停止触发器
                        scheduler.pauseTrigger(TriggerKey.triggerKey(trigger.getId()));
                        // 卸载任务
                        scheduler.unscheduleJob(TriggerKey.triggerKey(trigger.getId()));
                        // 删除任务
                        scheduler.deleteJob(JobKey.jobKey(trigger.getId()));
                    } catch (SchedulerException e) {
                        log.error("触发器删除失败: {}", e.getMessage());
                        throw new RuntimeException("触发器删除失败");
                    }
                    this.triggerRepository.deleteById(trigger.getId());
                });
    }

    public String getNextFireTime(String projectId) {
        var triggerId = this.triggerRepository.findByProjectId(projectId)
                .map(Trigger::getId)
                .orElse("");
        if (triggerId.isBlank()) {
            return triggerId;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            var date = scheduler.getTrigger(TriggerKey.triggerKey(triggerId)).getNextFireTime();
            return sdf.format(date);
        } catch (SchedulerException e) {
            log.info("未找到触发器： {}", e.getMessage());
            throw new RuntimeException("未找到触发器");
        }
    }

    public void startTriggers() {
        var triggers = this.triggerRepository.findCronTriggerAll();
        triggers.forEach(trigger -> {
            var cronTrigger = this.createCronTrigger(trigger);
            var jobDetail = this.createJobDetail(trigger);
            try {
                scheduler.scheduleJob(jobDetail, cronTrigger);
            } catch (SchedulerException e) {
                log.error("触发器加载失败: {}", e.getMessage());
                throw new RuntimeException("触发器加载失败");
            }
        });
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            log.error("触发器启动失败: {}", e.getMessage());
            throw new RuntimeException("触发器启动失败");
        }
    }

    private CronTrigger createCronTrigger(Trigger trigger) {
        var builder = CronScheduleBuilder.cronSchedule(trigger.getSchedule());
        return TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey(trigger.getId()))
                .usingJobData("triggerId", trigger.getId())
                .withSchedule(builder)
                .build();
    }

    private JobDetail createJobDetail(Trigger trigger) {
        return JobBuilder.newJob()
                .withIdentity(JobKey.jobKey(trigger.getId()))
                .ofType(PublishJob.class)
                .build();
    }
}
