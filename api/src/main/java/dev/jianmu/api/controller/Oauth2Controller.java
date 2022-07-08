package dev.jianmu.api.controller;

import dev.jianmu.api.dto.AuthorizationUrlGettingDto;
import dev.jianmu.api.dto.JwtResponse;
import dev.jianmu.api.dto.Oauth2LoggingDto;
import dev.jianmu.api.jwt.JwtProvider;
import dev.jianmu.api.jwt.JwtSession;
import dev.jianmu.api.oauth2_api.OAuth2Api;
import dev.jianmu.api.oauth2_api.config.OAuth2Properties;
import dev.jianmu.api.oauth2_api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.api.oauth2_api.exception.NoPermissionException;
import dev.jianmu.api.oauth2_api.impl.OAuth2ApiProxy;
import dev.jianmu.api.oauth2_api.vo.IRepoMemberVo;
import dev.jianmu.api.oauth2_api.vo.IRepoVo;
import dev.jianmu.api.oauth2_api.vo.IUserInfoVo;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.api.vo.AuthorizationUrlVo;
import dev.jianmu.api.vo.ThirdPartyTypeVo;
import dev.jianmu.application.exception.*;
import dev.jianmu.infrastructure.jwt.JwtProperties;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
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
public class Oauth2Controller {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2Properties oAuth2Properties;

    public Oauth2Controller(UserRepository userRepository, AuthenticationManager authenticationManager, JwtProvider jwtProvider, JwtProperties jwtProperties, OAuth2Properties oAuth2Properties) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
        this.oAuth2Properties = oAuth2Properties;
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
     * @param oauth2LoggingDto
     * @return
     */
    @GetMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid Oauth2LoggingDto oauth2LoggingDto) {
        this.beforeAuthenticate();
        this.allowOrNotRegistration();
        this.allowThisPlatformLogIn(oauth2LoggingDto.getThirdPartyType());

        OAuth2Api oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(oauth2LoggingDto.thirdPartyType())
                .build();

        String accessToken = oAuth2Api.getAccessToken(oauth2LoggingDto.getCode(), oauth2LoggingDto.getRedirectUri());
        IUserInfoVo userInfoVo = oAuth2Api.getUserInfo(accessToken);

        JwtSession.Role role = null;
        IRepoVo repo = null;
        try {
            if (this.oAuth2Properties.isEntry()) {
                repo = this.checkEntry(accessToken, oauth2LoggingDto.getGitRepo(), oauth2LoggingDto.getGitRepoOwner(), oAuth2Api);
                role = this.mappingPermissions(oAuth2Api, userInfoVo, accessToken, oauth2LoggingDto.getGitRepo(), oauth2LoggingDto.getGitRepoOwner(), oauth2LoggingDto.getThirdPartyType());
            }
        } catch (OAuth2IsNotAuthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(JwtResponse.builder()
                    .message(e.getMessage())
                    .build()
            );
        }

        Optional<User> userOptional = this.userRepository.findById(userInfoVo.getId());
        User user;
        if (userOptional.isEmpty()) {
            user = User.Builder.aReference()
                    .data(userInfoVo.getData())
                    .id(userInfoVo.getId())
                    .avatarUrl(userInfoVo.getAvatarUrl())
                    .username(userInfoVo.getUsername())
                    .nickname(userInfoVo.getNickname())
                    .build();
            this.userRepository.add(user);
        } else {
            user = userOptional.get();
        }

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(JsonUtil.jsonToString(JwtSession.builder()
                        .avatarUrl(user.getAvatarUrl())
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .gitRepoRole(role)
                        .gitRepo(repo != null ? repo.getRepo() : null)
                        .gitRepoOwner(repo != null ? repo.getOwner() : null)
                        .gitRepoId(repo != null ? repo.getId() : null)
                        .build()),
                        this.jwtProperties.getPassword(this.oAuth2Properties.getClientSecret())));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = this.jwtProvider.generateJwtToken(authentication);

        return ResponseEntity.ok(JwtResponse.builder()
                .type("Bearer")
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .gitRepo(repo != null ? repo.getRepo() : null)
                .gitRepoOwner(repo != null ? repo.getOwner() : null)
                .gitRepoId(repo != null ? repo.getId() : null)
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
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

    /**
     * 准入配置是否正确
     */
    private IRepoVo checkEntry(String accessToken, String gitRepo, String gitRepoOwner, OAuth2Api oAuth2Api) {
        if (StringUtils.hasLength(gitRepo) && StringUtils.hasLength(gitRepoOwner)) {
            IRepoVo repo;
            try {
                repo = oAuth2Api.getRepo(accessToken, gitRepo, gitRepoOwner);
                if (repo != null) {
                    return repo;
                }
            }catch (NoPermissionException e){
                throw new OAuth2IsNotAuthorizedException(e.getMessage());
            }
            throw new OAuth2EntryException("不存在此仓库");
        }
        throw new OAuth2EntryException("缺少仓库名或仓库所有者信息");
    }

    /**
     * 将其他平台的权限映射成内部的权限
     *
     * @param oAuth2Api
     * @param userInfoVo
     * @return
     */
    private JwtSession.Role mappingPermissions(OAuth2Api oAuth2Api, IUserInfoVo userInfoVo, String accessToken, String gitRepo, String gitRepoOwner, String thirdPartyType) {
        List<? extends IRepoMemberVo> repoMembers = oAuth2Api.getRepoMembers(accessToken, gitRepo, gitRepoOwner);
        for (IRepoMemberVo member : repoMembers) {
            if (member.getUsername().equals(userInfoVo.getUsername())) {
                if (member.isOwner()) {
                    return JwtSession.Role.OWNER;
                } else if (member.isAdmin()) {
                    return JwtSession.Role.ADMIN;
                } else {
                    return JwtSession.Role.MEMBER;
                }
            }
        }
        throw new OAuth2IsNotAuthorizedException("没有权限操作此仓库");
    }
}
