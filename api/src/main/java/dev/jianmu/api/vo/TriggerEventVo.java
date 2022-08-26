package dev.jianmu.api.vo;

import dev.jianmu.trigger.aggregate.WebhookEvent;
import dev.jianmu.trigger.event.TriggerEventParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Daihw
 * @class TriggerEventVo
 * @description TriggerEventVo
 * @create 2022/8/25 3:20 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerEventVo {
    private String id;
    private String projectId;
    private String triggerId;
    private String webRequestId;
    private String triggerType;
    // Payload
    private String payload;
    // 触发时间
    private LocalDateTime occurredTime;
    private List<TriggerEventParameter> parameters;
    private WebhookParamVo.WebhookEventVo webhookEvent;

    public void setWebhookEvent(WebhookParamVo.WebhookEventVo webhookEvent) {
        this.webhookEvent = webhookEvent;
    }
}
