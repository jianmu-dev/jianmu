package dev.jianmu.api.jwt;

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

/**
 * @class WebSecurityConfig
 * @description WebSecurityConfig
 * @author Ethan Liu
 * @create 2021-05-18 08:53
*/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnProperty(prefix = "jianmu", name = "auth-mode", havingValue = "true")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthTokenFilter jwtAuthTokenFilter;

    private static final String[] AUTH_WHITELIST = {
            // -- swagger ui
            "/swagger-ui.html",
            "/swagger-ui/*",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v3/api-docs/**",
            "/webhook/**",
            "/webjars/**",
            "/workers/**"
    };

    public WebSecurityConfig(JwtUserDetailsService jwtUserDetailsService, JwtAuthEntryPoint jwtAuthEntryPoint, JwtAuthTokenFilter jwtAuthTokenFilter) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
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
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(jwtAuthEntryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests().antMatchers("/auth/**").permitAll()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated();
        http.addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
