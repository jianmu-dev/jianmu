package dev.jianmu.oauth2.api.impl.vo.gitlab;

import dev.jianmu.oauth2.api.vo.ITokenVo;
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
public class TokenVo implements ITokenVo {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private String scope;
    private long created_at;
    private long expires_in;

    @Override
    public String getAccessToken() {
        return this.access_token;
    }

    @Override
    public long getExpireInMs() {
        return expires_in * 1000;
    }
}