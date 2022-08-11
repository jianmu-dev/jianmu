package dev.jianmu.api.vo;

import dev.jianmu.trigger.aggregate.custom.webhook.CustomWebhookRule;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WebhookParamOperatorVo")
public class WebhookParamOperatorVo {
    private Parameter.Type type;
    private List<WebhookEventOperatorVo> operators;
}
