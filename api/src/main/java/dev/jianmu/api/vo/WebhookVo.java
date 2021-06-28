package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class: WebhookVo
 * @description: WebhookVo
 * @author: Ethan Liu
 * @create: 2021-06-28 16:39
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "WebhookVo")
public class WebhookVo {
    private String webhook;
}
