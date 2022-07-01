
package dev.jianmu.api.oauth2_api.impl.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GiteeTokenVo
 * @description 请求gitee的token的vo
 * @create 2021-06-30 14:08
 */
@Getter
@Setter
public class GiteeTokenVo {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;
}