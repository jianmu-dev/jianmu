package dev.jianmu.oauth2.api.impl.dto.gitlab;

/**
 * @author huangxi
 * @class LoggingDto
 * @description LoggingDto
 * @create 2022-08-10 15:53
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LoggingDto {
    private String grant_type;
    private String code;
    private String client_id;
    private String redirect_uri;
    private String client_secret;
}
