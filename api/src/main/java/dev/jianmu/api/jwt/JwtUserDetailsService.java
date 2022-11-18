package dev.jianmu.api.jwt;

import dev.jianmu.infrastructure.jackson2.JsonUtil;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author huangxi
 * @class JwtUserDetailsService
 * @description JwtUserDetailsService
 * @create 2022-06-30 15:06
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final OAuth2Properties oAuth2Properties;
    private final JwtProperties jwtProperties;

    public JwtUserDetailsService(OAuth2Properties oAuth2Properties, JwtProperties jwtProperties) {
        this.oAuth2Properties = oAuth2Properties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String jwtSession) throws UsernameNotFoundException {
        JwtSession session = JsonUtil.stringToJson(jwtSession, JwtSession.class);
        return JwtUserDetails.build(session, this.jwtProperties.getEncryptedPassword(this.oAuth2Properties.getClientSecret()));
    }
}
