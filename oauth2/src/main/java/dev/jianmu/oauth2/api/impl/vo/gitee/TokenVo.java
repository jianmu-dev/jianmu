package dev.jianmu.oauth2.api.impl.vo.gitee;

import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.UnknownException;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
import dev.jianmu.oauth2.api.util.ApplicationContextUtil;
import dev.jianmu.oauth2.api.vo.ITokenVo;
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
public class TokenVo implements ITokenVo {
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;

    @Override
    public String getAccessToken() {
        return this.access_token;
    }

    @Override
    public long getExpireInMs() {
        return this.expires_in * 1000;
    }
}