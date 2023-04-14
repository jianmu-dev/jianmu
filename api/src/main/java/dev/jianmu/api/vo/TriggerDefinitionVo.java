package dev.jianmu.api.vo;

import dev.jianmu.trigger.aggregate.WebhookAuth;
import dev.jianmu.trigger.aggregate.WebhookParameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @class TriggerDefinitionVo
 * @description TriggerDefinitionVo
 * @author Daihw
 * @create 2023/4/14 9:18 上午
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "触发器定义Vo")
public class TriggerDefinitionVo {
    @Schema(description = "webhook参数")
    private List<WebhookParameter> params;

    @Schema(description = "auth")
    private WebhookAuth auth;

    @Schema(description = "only")
    private String only;

    @Schema(description = "最近请求ID")
    private String latestWebRequestId;
}
