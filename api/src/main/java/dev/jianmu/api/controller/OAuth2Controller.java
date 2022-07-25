package dev.jianmu.api.controller;

import dev.jianmu.api.dto.AuthorizationUrlGettingDto;
import dev.jianmu.api.dto.JwtResponse;
import dev.jianmu.api.dto.impl.GitRepoLoggingDto;
import dev.jianmu.api.jwt.JwtProvider;
import dev.jianmu.api.jwt.JwtSession;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.api.vo.AuthorizationUrlVo;
import dev.jianmu.api.vo.ThirdPartyTypeVo;
import dev.jianmu.application.exception.NotAllowRegistrationException;
import dev.jianmu.application.exception.NotAllowThisPlatformLogInException;
import dev.jianmu.application.exception.OAuth2IsNotAuthorizedException;
import dev.jianmu.application.exception.OAuth2IsNotConfiguredException;
import dev.jianmu.application.service.OAuth2Application;
import dev.jianmu.application.service.vo.Association;
import dev.jianmu.application.service.vo.AssociationData;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.oauth2.api.vo.ITokenVo;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import dev.jianmu.user.aggregate.User;
import dev.jianmu.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

/**
 * @author huangxi
 * @class AuthController
 * @description oauth2控制器
 * @create 2021-06-30 14:08
 */
@RestController
@RequestMapping("auth/oauth2")
@Tag(name = "oauth2控制器", description = "oauth2控制器")
public class OAuth2Controller {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2Properties oAuth2Properties;
    private final AssociationUtil associationUtil;
    private final OAuth2Application oAuth2Application;

    public OAuth2Controller(UserRepository userRepository, AuthenticationManager authenticationManager, JwtProvider jwtProvider, JwtProperties jwtProperties, OAuth2Properties oAuth2Properties, AssociationUtil associationUtil, OAuth2Application oAuth2Application) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
        this.oAuth2Properties = oAuth2Properties;
        this.associationUtil = associationUtil;
        this.oAuth2Application = oAuth2Application;
    }

    /**
     * 获取授权url
     */
    @GetMapping("url")
    @Operation(summary = "获取授权url", description = "获取授权url")
    public AuthorizationUrlVo getAuthorizationUrl(@Valid AuthorizationUrlGettingDto authorizationUrlGettingDto) {
        this.beforeAuthenticate();
        this.allowThisPlatformLogIn(authorizationUrlGettingDto.getThirdPartyType());

        OAuth2Api oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(authorizationUrlGettingDto.thirdPartyType())
                .build();
        return AuthorizationUrlVo.builder()
                .authorizationUrl(oAuth2Api.getAuthUrl(authorizationUrlGettingDto.getRedirectUri()))
                .build();
    }

    /**
     * 获取jwt
     *
     * @param gitRepoLoggingDto
     * @return
     */
    @GetMapping("/login/git_repo")
    @Transactional
    public ResponseEntity<JwtResponse> authenticateUser(@Valid GitRepoLoggingDto gitRepoLoggingDto) {
        this.beforeAuthenticate();
        this.allowOrNotRegistration();
        this.allowThisPlatformLogIn(gitRepoLoggingDto.getThirdPartyType());

        OAuth2Api oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(gitRepoLoggingDto.thirdPartyType())
                .build();

        ITokenVo tokenVo = oAuth2Api.getAccessToken(gitRepoLoggingDto.getCode(), gitRepoLoggingDto.getRedirectUri());
        String accessToken = tokenVo.getAccessToken();
        String encryptedToken = tokenVo.getEncryptedAccessToken();
        long expireInMs = tokenVo.getExpireInMs();

        IUserInfoVo userInfoVo = oAuth2Api.getUserInfo(accessToken);

        Association association;
        try {
            association = this.oAuth2Application.getAssociation(gitRepoLoggingDto.thirdPartyType(), accessToken, userInfoVo,
                    AssociationData.buildGitRepo(gitRepoLoggingDto.getRef(), gitRepoLoggingDto.getOwner()));
        } catch (OAuth2IsNotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(JwtResponse.builder()
                    .message(e.getMessage())
                    .build()
            );
        }

        String userId = userInfoVo.getId();
        Optional<User> userOptional = this.userRepository.findById(userId);
        User user = User.Builder.aReference()
                .data(userInfoVo.getData())
                .id(userId)
                .avatarUrl(userInfoVo.getAvatarUrl())
                .username(userInfoVo.getUsername())
                .nickname(userInfoVo.getNickname())
                .build();
        if (userOptional.isEmpty()) {
            this.userRepository.add(user);
        } else {
            this.userRepository.update(user);
        }

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(JsonUtil.jsonToString(JwtSession.builder()
                        .avatarUrl(user.getAvatarUrl())
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .role(association.getRole())
                        .associationId(association.getId())
                        .associationType(association.getType())
                        .encryptedToken(encryptedToken)
                        .expireTimestamp(System.currentTimeMillis() + expireInMs)
                        .build()),
                        this.jwtProperties.getPassword(this.oAuth2Properties.getClientSecret())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = this.jwtProvider.generateJwtToken(authentication, expireInMs);

        return ResponseEntity.ok(JwtResponse.builder()
                .type("Bearer")
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .associationId(association.getId())
                .associationType(association.getType())
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .entryUrl(oAuth2Api.getEntryUrl(gitRepoLoggingDto.getOwner(), gitRepoLoggingDto.getRef()))
                .build());
    }

    /**
     * 返回现在允许登录的第三方平台
     *
     * @return
     */
    @GetMapping("third_party_type")
    public ThirdPartyTypeVo getThirdPartyType() {
        return ThirdPartyTypeVo.builder()
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .entry(this.oAuth2Properties.isEntry())
                .build();
    }

    /**
     * 认证之前
     */
    private void beforeAuthenticate() {
        if (this.oAuth2Properties.getGitee() != null || this.oAuth2Properties.getGitlink() != null) {
            return;
        }
        throw new OAuth2IsNotConfiguredException("未配置OAuth2登录");
    }

    /**
     * oauth2是否允许注册
     */
    private void allowOrNotRegistration() {
        if (this.oAuth2Properties.isAllowRegistration()) {
            return;
        }
        throw new NotAllowRegistrationException("目前不允许使用OAuth2登录，请与管理员联系");
    }

    /**
     * 是否允许此平台登录
     *
     * @param thirdPartyType
     */
    private void allowThisPlatformLogIn(String thirdPartyType) {
        if (this.oAuth2Properties.getGitee() != null && ThirdPartyTypeEnum.GITEE.name().equals(thirdPartyType)
                ||
                this.oAuth2Properties.getGitlink() != null && ThirdPartyTypeEnum.GITLINK.name().equals(thirdPartyType)) {
            return;
        }
        throw new NotAllowThisPlatformLogInException("不允许" + thirdPartyType + "平台登录，请与管理员联系");
    }
}
