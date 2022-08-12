package dev.jianmu.oauth2.api.impl.dto.gitlink;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author huangxi
 * @class WebhookCreatingDto
 * @description WebhookCreatingDto
 * @create 2022-07-26 17:37
 */
@Builder
@Getter
@Setter
public class WebhookCreatingDto {
    private final String content_type = "json";
    private final String http_method = "POST";
    private final List<String> events = List.of("push", "pull_request_only");
    private String url;
    private boolean active;
}
