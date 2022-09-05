package dev.jianmu.api.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.exception.JsonParseException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class UserContextHolder {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final GlobalProperties globalProperties;
    private final JwtProperties jwtProperties;

    public UserContextHolder(GlobalProperties globalProperties, JwtProperties jwtProperties) {
        this.globalProperties = globalProperties;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 获取用户
     *
     * @return
     */
    public JwtSession getSession() {
        String authorizationHeader = (((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest())
                .getHeader("Authorization");
        if (!this.globalProperties.getAuthMode() && authorizationHeader == null) {
            return new JwtSession();
        }
        authorizationHeader = authorizationHeader.substring(7);
        String userJson = Jwts.parserBuilder()
                .setSigningKey(this.jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(authorizationHeader)
                .getBody()
                .getSubject();
        try {
            return mapper.readValue(userJson, JwtSession.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
    }
}

