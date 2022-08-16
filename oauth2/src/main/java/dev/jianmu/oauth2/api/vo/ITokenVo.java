package dev.jianmu.oauth2.api.vo;

import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.exception.UnknownException;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
import dev.jianmu.oauth2.api.util.ApplicationContextUtil;

/**
 * @author huangxi
 * @class ITokenVo
 * @description ITokenVo
 * @create 2022-07-19 13:59
 */
public interface ITokenVo {
    String getAccessToken();

    long getExpireInMs();

    default String getEncryptedAccessToken() {
        try {
            return AESEncryptionUtil.encrypt(getAccessToken(), ApplicationContextUtil.getBean(OAuth2Properties.class).getClientSecret());
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnknownException(e.getMessage());
        }
    }
}
