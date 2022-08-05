package dev.jianmu.api.jwt;

import dev.jianmu.oauth2.api.config.OAuth2Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ethan Liu
 * @class WebSecurityConfig
 * @description WebSecurityConfig
 * @create 2021-05-18 08:53
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(prefix = "jianmu", name = "auth-mode", havingValue = "true", matchIfMissing = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final List<String> AUTH_WHITELIST = new ArrayList<>();

    static {
        // 在此list中的请求路径一律放行
        AUTH_WHITELIST.add("/swagger-ui.html");
        AUTH_WHITELIST.add("/swagger-ui/*");
        AUTH_WHITELIST.add("/swagger-resources/**");
        AUTH_WHITELIST.add("/v2/api-docs");
        AUTH_WHITELIST.add("/v3/api-docs/**");
        AUTH_WHITELIST.add("/webhook/**");
        AUTH_WHITELIST.add("/workers/**");
        AUTH_WHITELIST.add("/workers/**");
    }

    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthTokenFilter jwtAuthTokenFilter;
    private final OAuth2Properties oAuth2Properties;

    public WebSecurityConfig(JwtUserDetailsService jwtUserDetailsService, JwtAuthEntryPoint jwtAuthEntryPoint, JwtAuthTokenFilter jwtAuthTokenFilter, OAuth2Properties oAuth2Properties) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
        this.oAuth2Properties = oAuth2Properties;
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (this.oAuth2Properties.getType() == null) {
            AUTH_WHITELIST.add("/view/**");
        }
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/auth/**").permitAll()
                .antMatchers(AUTH_WHITELIST.toArray(new String[0])).permitAll()
                .anyRequest().authenticated();
        http.addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
