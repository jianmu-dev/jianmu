package dev.jianmu.api.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final JwtProperties jwtProperties;

    public UserContextHolder(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 获取用户
     *
     * @return
     */
    public JwtSession getSession() {
        String jwt = (((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest())
                .getHeader("Authorization")
                .substring(7);
        String userJson = Jwts.parserBuilder()
                .setSigningKey(this.jwtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
        try {
            return mapper.readValue(userJson, JwtSession.class);
        } catch (JsonProcessingException e) {
            throw new JsonParseException();
        }
    }
}

