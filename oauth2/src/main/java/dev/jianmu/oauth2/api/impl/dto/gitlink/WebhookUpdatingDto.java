package dev.jianmu.oauth2.api.impl.dto.gitlink;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author huangxi
 * @class WebhookUpdatingDto
 * @description WebhookUpdatingDto
 * @create 2022-07-26 18:13
 */
@Builder
@Getter
@Setter
public class WebhookUpdatingDto {
    private final String content_type = "json";
    private final String http_method = "POST";
    private final List<String> events = List.of("push", "pull_request_only");
    private String url;
    private boolean active;
}
