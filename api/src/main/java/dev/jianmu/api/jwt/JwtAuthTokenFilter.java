package dev.jianmu.api.jwt;

import dev.jianmu.infrastructure.jackson2.JsonUtil;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author Ethan Liu
 * @class JwtAuthTokenFilter
 * @description JwtAuthTokenFilter
 * @create 2021-05-17 20:48
 */
@Component
public class JwtAuthTokenFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final OAuth2Properties oAuth2Properties;
    private final JwtProperties jwtProperties;

    public JwtAuthTokenFilter(JwtProvider jwtProvider, OAuth2Properties oAuth2Properties, JwtProperties jwtProperties) {
        this.jwtProvider = jwtProvider;
        this.oAuth2Properties = oAuth2Properties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && this.jwtProvider.validateJwtToken(jwt)) {
                String jwtSession = this.jwtProvider.getUsernameFromToken(jwt);
                JwtUserDetails userDetails = new JwtUserDetails(JsonUtil.stringToJson(jwtSession, JwtSession.class),
                        this.jwtProperties.getEncryptedPassword(this.oAuth2Properties.getClientSecret()));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String newJwt = this.jwtProvider.generateJwtToken(authentication);
                response.setHeader("X-Authorization-Token", newJwt);
            }
        } catch (RuntimeException e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }

        return null;
    }
}
