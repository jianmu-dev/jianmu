
package dev.jianmu.oauth2.api.impl.vo.gitlab;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class TokenVo
 * @description 请求gitlab的token的vo
 * @create 2021-08-10 16:08
 */
@Getter
@Setter
public class TokenVo {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private String scope;
    private long created_at;
    private long expires_in;
}