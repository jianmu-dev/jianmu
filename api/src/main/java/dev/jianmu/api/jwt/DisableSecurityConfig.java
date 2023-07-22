package dev.jianmu.api.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * @author Ethan Liu
 * @class DisableSecurityConfig
 * @description DisableSecurityConfig
 * @create 2022-03-26 15:31
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(prefix = "jianmu", name = "auth-mode", havingValue = "false")
public class DisableSecurityConfig {

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return null;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().anyRequest();
    }

}
