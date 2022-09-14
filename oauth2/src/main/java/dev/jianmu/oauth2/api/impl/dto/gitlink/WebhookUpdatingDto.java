package dev.jianmu.oauth2.api.impl.dto.gitlink;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("content_type")
    private final String content_type = "json";
    @JsonProperty("http_method")
    private final String http_method = "POST";
    private List<String> events;
    private String url;
    private boolean active;
}
