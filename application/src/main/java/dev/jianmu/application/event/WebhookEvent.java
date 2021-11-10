package dev.jianmu.application.event;

import dev.jianmu.trigger.aggregate.Webhook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @class: WebhookEvent
 * @description: WebhookEvent
 * @author: Ethan Liu
 * @create: 2021-11-10 23:07
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEvent {
    private String projectId;
    private Webhook webhook;
}
