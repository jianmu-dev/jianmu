package dev.jianmu.application.dsl.webhook;

import dev.jianmu.trigger.aggregate.WebhookAuth;
import lombok.Getter;

import java.util.List;

@Getter
public class Webhook {
    private List<DslWebhookParameter> param;
    private WebhookAuth auth;
    private String only;
}
