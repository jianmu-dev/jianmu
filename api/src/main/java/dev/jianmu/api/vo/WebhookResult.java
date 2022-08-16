package dev.jianmu.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class WebhookResult
 * @description WebhookResult
 * @create 2022/8/15 4:42 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResult {
    private String projectId;
    private String triggerId;
}
