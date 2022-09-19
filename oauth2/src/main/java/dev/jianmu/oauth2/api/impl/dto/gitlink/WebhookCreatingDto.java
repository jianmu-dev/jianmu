package dev.jianmu.oauth2.api.impl.dto.gitlink;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.oauth2.api.exception.JsonParseException;
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
    @JsonProperty("content_type")
    private final String contentType = "json";
    @JsonProperty("http_method")
    private final String httpMethod = "POST";
    private List<String> events;
    private String url;
    private boolean active;
}
