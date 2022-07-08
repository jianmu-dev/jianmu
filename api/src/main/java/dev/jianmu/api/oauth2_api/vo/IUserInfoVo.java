package dev.jianmu.api.oauth2_api.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.oauth2_api.exception.JsonParseException;

public interface IUserInfoVo {
    @JsonIgnore
    String getId();

    @JsonIgnore
    String getAvatarUrl();

    @JsonIgnore
    String getNickname();

    @JsonIgnore
    String getUsername();

    @JsonIgnore
    default String getData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
    }
}
