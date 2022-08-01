package dev.jianmu.api.controller;

import dev.jianmu.api.dto.JwtResponse;
import dev.jianmu.api.dto.LoginDto;
import dev.jianmu.api.jwt.JwtProvider;
import dev.jianmu.api.jwt.JwtSession;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.NotAllowAuthSignInException;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Ethan Liu
 * @class AuthController
 * @description AuthController
 * @create 2021-05-18 09:38
 */
@RestController
@RequestMapping("auth")
@Tag(name = "Auth", description = "Auth API")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public AuthController(AuthenticationManager authenticationManager, JwtProvider jwtProvider, UserRepository userRepository, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        this.beforeAuthenticate();
        var user = this.userRepository.findByName(loginDto.getUsername())
                .orElseThrow(() -> new DataNotFoundException("未找到该用户名"));

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(JsonUtil.jsonToString(JwtSession.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build()), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .type("Bearer")
                .build());
    }

    private void beforeAuthenticate() {
        if (!this.jwtProperties.checkAdminPassword()) {
            throw new NotAllowAuthSignInException("不允许使用用户名密码的方式登录，请与管理员联系");
        }
    }
}
