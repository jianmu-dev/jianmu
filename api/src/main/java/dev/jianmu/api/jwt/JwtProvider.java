package dev.jianmu.api.jwt;

import dev.jianmu.infrastructure.GlobalProperties;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Ethan Liu
 * @class JwtProvider
 * @description JwtProvider
 * @create 2021-05-17 21:02
 */
@Component
public class JwtProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private final GlobalProperties globalProperties;

    public JwtProvider(GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    public String generateJwtToken(Authentication authentication) {
        JwtUserDetails userPrincipal = (JwtUserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + globalProperties.getApi().getJwtExpirationMs()))
                .signWith(SignatureAlgorithm.HS512, globalProperties.getApi().getJwtSecret())
                .compact();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(globalProperties.getApi().getJwtSecret()).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser().setSigningKey(globalProperties.getApi().getJwtSecret()).parseClaimsJws(token).getBody().getSubject();
    }
}
