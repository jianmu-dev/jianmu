package dev.jianmu.application.dsl;

import dev.jianmu.trigger.aggregate.WebhookAuth;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import lombok.*;

import java.util.List;

/**
 * @class: Webhook
 * @description: Webhook
 * @author: Ethan Liu
 * @create: 2021-11-10 09:02
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Webhook {
    private List<WebhookParameter> param;
    private WebhookAuth auth;
    private String matcher;
}
