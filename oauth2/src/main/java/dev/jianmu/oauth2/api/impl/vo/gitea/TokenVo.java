package dev.jianmu.oauth2.api.impl.vo.gitea;

import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class TokenVo
 * @description TokenVo
 * @create 2022-08-24 15:01
 */
@Getter
@Setter
public class TokenVo {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private long expires_in;
}
