
package dev.jianmu.api.oauth2_api.impl.vo.gitlink;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class GitlinkTokenVo
 * @description 请求gitlink的token的vo
 * @create 2021-07-05 18:11
 */
@Getter
@Setter
public class TokenVo {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;
}