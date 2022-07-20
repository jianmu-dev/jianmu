package dev.jianmu.application.dsl.webhook;

import dev.jianmu.trigger.aggregate.WebhookAuth;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import lombok.Getter;

import java.util.List;

@Getter
public class Webhook {
    private List<WebhookParameter> param;
    private WebhookAuth auth;
    private String only;

    public void setParam(List<WebhookParameter> param) {
        this.param = param;
    }
}
