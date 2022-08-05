package dev.jianmu.api.eventhandler;

import dev.jianmu.application.event.CronEvent;
import dev.jianmu.application.event.ManualEvent;
import dev.jianmu.application.event.WebhookEvent;
import dev.jianmu.application.service.CustomWebhookDefinitionApplication;
import dev.jianmu.application.service.ProjectApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.infrastructure.quartz.CronTriggerEvent;
import dev.jianmu.trigger.event.CustomWebhookInstanceEvent;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.event.TriggerFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * @author Ethan Liu
 * @class TriggerEventHandler
 * @description TriggerEventHandler
 * @create 2021-05-25 08:44
 */
@Component
@Slf4j
public class TriggerEventHandler {
    private final ProjectApplication projectApplication;
    private final TriggerApplication triggerApplication;
    private final CustomWebhookDefinitionApplication webhookDefinitionApplication;

    public TriggerEventHandler(ProjectApplication projectApplication,
                               TriggerApplication triggerApplication,
                               CustomWebhookDefinitionApplication webhookDefinitionApplication
    ) {
        this.projectApplication = projectApplication;
        this.triggerApplication = triggerApplication;
        this.webhookDefinitionApplication = webhookDefinitionApplication;
    }

    @EventListener
    public void handleCronTriggerEvent(CronTriggerEvent cronTriggerEvent) {
        // 处理quartz触发的事件
        log.info("Got CronTriggerEvent: {} at: {}", cronTriggerEvent, LocalDateTime.now());
        this.triggerApplication.trigger(cronTriggerEvent.getTriggerId());
    }

    @Async
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleWebhookEvent(WebhookEvent event) {
        // 创建Webhook触发器
        this.triggerApplication.saveOrUpdate(event.getProjectId(), event.getWebhook(), event.getEncryptedToken(), event.getUserId(), event.getWebhookType(), event.getEventInstances());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleManualEvent(ManualEvent manualEvent) {
        // 删除相关触发器
        this.triggerApplication.deleteByProjectId(manualEvent.getProjectId(), manualEvent.getEncryptedToken(), manualEvent.getAssociationId(), manualEvent.getAssociationType(), manualEvent.getUserId());
    }

    @Async
    @EventListener
    public void handleTriggerFailedEvent(TriggerFailedEvent triggerFailedEvent) {
        // 修改Webhook状态
        this.triggerApplication.updateTriggerStatus(triggerFailedEvent.getTriggerId(), triggerFailedEvent.getTriggerType());
    }

    @EventListener
    public void handleCustomWebhookInstanceEvent(CustomWebhookInstanceEvent event) {
        // 创建修改自定义Webhook实例
        this.webhookDefinitionApplication.saveOrUpdateWebhookInstance(event.getTriggerId(), event.getWebhook(), event.getEventInstances());
    }
}
