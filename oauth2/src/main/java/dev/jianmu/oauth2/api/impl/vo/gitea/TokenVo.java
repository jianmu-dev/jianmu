package dev.jianmu.oauth2.api.impl.vo.gitea;

import dev.jianmu.oauth2.api.vo.ITokenVo;
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
public class TokenVo implements ITokenVo {
    private String access_token;
    private String token_type;
    private String refresh_token;
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
