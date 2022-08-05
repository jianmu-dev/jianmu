package dev.jianmu.api.vo;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookInstance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WebhookOperatorVo")
public class WebhookOperatorVo {
    private List<WebhookParamOperatorVo> paramOperators;
    private List<WebhookEventOperatorVo> rulesetOperators;
}
