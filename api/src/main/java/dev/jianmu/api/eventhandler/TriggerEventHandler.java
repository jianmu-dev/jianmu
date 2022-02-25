package dev.jianmu.api.eventhandler;

import dev.jianmu.application.event.CronEvent;
import dev.jianmu.application.event.ManualEvent;
import dev.jianmu.application.event.WebhookEvent;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.infrastructure.quartz.CronTriggerEvent;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.event.TriggerFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * @class TriggerEventHandler
 * @description TriggerEventHandler
 * @author Ethan Liu
 * @create 2021-05-25 08:44
*/
@Component
@Slf4j
public class TriggerEventHandler {
    private final ProjectApplication projectApplication;
    private final TriggerApplication triggerApplication;

    public TriggerEventHandler(ProjectApplication projectApplication, TriggerApplication triggerApplication) {
        this.projectApplication = projectApplication;
        this.triggerApplication = triggerApplication;
    }

    @EventListener
    public void handleCronTriggerEvent(CronTriggerEvent cronTriggerEvent) {
        // 处理quartz触发的事件
        log.info("Got CronTriggerEvent: {} at: {}", cronTriggerEvent, LocalDateTime.now());
        this.triggerApplication.trigger(cronTriggerEvent.getTriggerId());
    }

    @EventListener
    public void handleTriggerEvent(TriggerEvent triggerEvent) {
        // 触发项目模块
        log.info("Got TriggerEvent: {} at: {}", triggerEvent, triggerEvent.getOccurredTime());
        this.projectApplication.trigger(triggerEvent.getProjectId(), triggerEvent.getId(), triggerEvent.getTriggerType());
    }

    @TransactionalEventListener
    public void handleCronEvent(CronEvent cronEvent) {
        // 创建Cron触发器
        this.triggerApplication.saveOrUpdate(cronEvent.getProjectId(), cronEvent.getSchedule());
    }

    @TransactionalEventListener
    public void handleWebhookEvent(WebhookEvent webhookEvent) {
        // 创建Webhook触发器
        this.triggerApplication.saveOrUpdate(webhookEvent.getProjectId(), webhookEvent.getWebhook());
    }

    @TransactionalEventListener
    public void handleManualEvent(ManualEvent manualEvent) {
        // 删除相关触发器
        this.triggerApplication.deleteByProjectId(manualEvent.getProjectId());
    }

    @EventListener
    public void handleTriggerFailedEvent(TriggerFailedEvent triggerFailedEvent) {
        // 修改Webhook状态
        this.triggerApplication.updateTriggerStatus(triggerFailedEvent.getTriggerId(), triggerFailedEvent.getTriggerType());
    }
 }
