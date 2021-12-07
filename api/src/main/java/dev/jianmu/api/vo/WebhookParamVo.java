package dev.jianmu.api.vo;

import dev.jianmu.application.dsl.webhook.WebhookParameter;
import dev.jianmu.trigger.aggregate.WebhookAuth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Daihw
 * @class WebhookParamVo
 * @description WebhookParamVo
 * @create 2021/12/6 5:03 下午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookParamVo {
    @Schema(description = "参数")
    private List<WebhookParameter> param;
    @Schema(description = "auth")
    private WebhookAuth auth;
    @Schema(description = "only")
    private String only;
}
