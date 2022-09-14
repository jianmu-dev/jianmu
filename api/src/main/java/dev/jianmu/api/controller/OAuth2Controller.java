package dev.jianmu.api.controller;

import dev.jianmu.api.dto.AuthorizationUrlGettingDto;
import dev.jianmu.api.dto.GitRepoTokenRefreshingDto;
import dev.jianmu.api.dto.JwtResponse;
import dev.jianmu.api.dto.Oauth2LoggingDto;
import dev.jianmu.api.dto.impl.GitRepoLoggingDto;
import dev.jianmu.api.jwt.JwtProvider;
import dev.jianmu.api.jwt.JwtSession;
import dev.jianmu.api.jwt.UserContextHolder;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.api.vo.AuthorizationUrlVo;
import dev.jianmu.api.vo.ThirdPartyTypeVo;
import dev.jianmu.application.exception.NotAllowRegistrationException;
import dev.jianmu.application.exception.NotAllowThisPlatformLogInException;
import dev.jianmu.application.exception.OAuth2IsNotConfiguredException;
import dev.jianmu.application.service.OAuth2Application;
import dev.jianmu.application.service.vo.AssociationData;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.OAuth2Api;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.RoleEnum;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
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
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    private final UserContextHolder userContextHolder;
    private final GitRepoRepository gitRepoRepository;
    private final GlobalProperties globalProperties;

    public OAuth2Controller(UserRepository userRepository, AuthenticationManager authenticationManager, JwtProvider jwtProvider, JwtProperties jwtProperties, OAuth2Properties oAuth2Properties, AssociationUtil associationUtil, OAuth2Application oAuth2Application, UserContextHolder userContextHolder, GitRepoRepository gitRepoRepository, GlobalProperties globalProperties) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.jwtProperties = jwtProperties;
        this.oAuth2Properties = oAuth2Properties;
        this.associationUtil = associationUtil;
        this.oAuth2Application = oAuth2Application;
        this.userContextHolder = userContextHolder;
        this.gitRepoRepository = gitRepoRepository;
        this.globalProperties = globalProperties;
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

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody Oauth2LoggingDto oauth2LoggingDto) {
        this.beforeAuthenticate();
        this.allowThisPlatformLogIn(oauth2LoggingDto.getThirdPartyType());

        OAuth2Api oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(oauth2LoggingDto.thirdPartyType())
                .build();

        ITokenVo tokenVo = oAuth2Api.getAccessToken(oauth2LoggingDto.getCode(), oauth2LoggingDto.getRedirectUri());
        String accessToken = tokenVo.getAccessToken();
        String encryptedToken = tokenVo.getEncryptedAccessToken();
        long expireInMs = tokenVo.getExpireInMs();

        IUserInfoVo userInfoVo = oAuth2Api.getUserInfo(accessToken);

        String userId = userInfoVo.getId();
        Optional<User> userOptional = this.userRepository.findById(userId);
        User user = User.Builder.aReference()
                .data(userInfoVo.getData())
                .id(userId)
                .avatarUrl(userInfoVo.getAvatarUrl() == null ?
                        "" : userInfoVo.getAvatarUrl())
                .username(userInfoVo.getUsername())
                .nickname(userInfoVo.getNickname())
                .build();
        if (userOptional.isEmpty()) {
            this.allowOrNotRegistration();
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
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .build());
    }

    /**
     * 获取jwt
     *
     * @param gitRepoLoggingDto
     * @return
     */
    @PostMapping("/login/git_repo")
    @Transactional
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody GitRepoLoggingDto gitRepoLoggingDto) {
        this.beforeAuthenticate();
        this.allowThisPlatformLogIn(gitRepoLoggingDto.getThirdPartyType());

        OAuth2Api oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(gitRepoLoggingDto.thirdPartyType())
                .build();

        ITokenVo tokenVo = oAuth2Api.getAccessToken(gitRepoLoggingDto.getCode(), gitRepoLoggingDto.getRedirectUri());
        String accessToken = tokenVo.getAccessToken();
        String encryptedToken = tokenVo.getEncryptedAccessToken();
        long expireInMs = tokenVo.getExpireInMs();

        IUserInfoVo userInfoVo = oAuth2Api.getUserInfo(accessToken);

        var associationData = AssociationData.buildGitRepo(gitRepoLoggingDto.getRef(), gitRepoLoggingDto.getOwner());
        var association = this.oAuth2Application.getAssociation(gitRepoLoggingDto.thirdPartyType(), accessToken, userInfoVo,
                associationData, null);

        String userId = userInfoVo.getId();
        Optional<User> userOptional = this.userRepository.findById(userId);
        User user = User.Builder.aReference()
                .data(userInfoVo.getData())
                .id(userId)
                .avatarUrl(userInfoVo.getAvatarUrl() == null ?
                        "" : userInfoVo.getAvatarUrl())
                .username(userInfoVo.getUsername())
                .nickname(userInfoVo.getNickname())
                .build();
        if (userOptional.isEmpty()) {
            this.allowOrNotRegistration();
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
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .entryUrl(oAuth2Api.getEntryUrl(gitRepoLoggingDto.getOwner(), gitRepoLoggingDto.getRef()))
                .associationData(associationData)
                .build());
    }

    /**
     * 刷新token
     *
     * @param gitRepoTokenRefreshingDto
     * @return
     */
    @PutMapping("/refresh/git_repo")
    @Transactional
    public ResponseEntity<?> refreshToken(@Valid @RequestBody GitRepoTokenRefreshingDto gitRepoTokenRefreshingDto) {
        JwtSession session = this.userContextHolder.getSession();

        String thirdPartyType = this.oAuth2Properties.getThirdPartyType();
        OAuth2ApiProxy oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(ThirdPartyTypeEnum.valueOf(thirdPartyType))
                .userId(session.getId())
                .build();

        IUserInfoVo userInfo;
        String accessToken;
        String encryptedToken;
        RoleEnum role;
        AssociationData associationData;

        encryptedToken = session.getEncryptedToken();
        try {
            accessToken = AESEncryptionUtil.decrypt(encryptedToken, this.oAuth2Properties.getClientSecret());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException("code有误，请检查");
        }
        userInfo = oAuth2Api.getUserInfo(accessToken);
        associationData = AssociationData.buildGitRepo(gitRepoTokenRefreshingDto.getRef(), gitRepoTokenRefreshingDto.getOwner());
        role = this.oAuth2Application.getAssociation(ThirdPartyTypeEnum.valueOf(thirdPartyType), accessToken, userInfo,
                associationData, null).getRole();


        Optional<GitRepo> gitRepoOptional = this.gitRepoRepository.findByRefAndOwner(gitRepoTokenRefreshingDto.getRef(), gitRepoTokenRefreshingDto.getOwner());
        if (gitRepoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var gitRepo = gitRepoOptional.get();
        long expireTimestamp = session.getExpireTimestamp();
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(JsonUtil.jsonToString(JwtSession.builder()
                        .avatarUrl(userInfo.getAvatarUrl())
                        .id(userInfo.getId())
                        .username(userInfo.getUsername())
                        .nickname(userInfo.getNickname())
                        .role(role)
                        .associationId(gitRepo.getId())
                        .associationType(this.associationUtil.getAssociationType())
                        .encryptedToken(encryptedToken)
                        .expireTimestamp(expireTimestamp)
                        .build()),
                        this.jwtProperties.getPassword(this.oAuth2Properties.getClientSecret())));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String newJwt = this.jwtProvider.generateJwtToken(authentication, expireTimestamp);
        return ResponseEntity.ok(JwtResponse.builder()
                .type("Bearer")
                .token(newJwt)
                .id(session.getId())
                .username(session.getUsername())
                .avatarUrl(session.getAvatarUrl())
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .entryUrl(oAuth2Api.getEntryUrl(gitRepoTokenRefreshingDto.getOwner(), gitRepoTokenRefreshingDto.getRef()))
                .associationData(associationData)
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
                .authMode(this.globalProperties.getAuthMode())
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .associationType(this.associationUtil.getAssociationType())
                .build();
    }

    /**
     * 认证之前
     */
    private void beforeAuthenticate() {
        if (this.oAuth2Properties.getGitee() != null
                || this.oAuth2Properties.getGitlink() != null
                || this.oAuth2Properties.getGitlab() != null
                || this.oAuth2Properties.getGitea() != null) {
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
                || this.oAuth2Properties.getGitlink() != null && ThirdPartyTypeEnum.GITLINK.name().equals(thirdPartyType)
                || this.oAuth2Properties.getGitlab() != null && ThirdPartyTypeEnum.GITLAB.name().equals(thirdPartyType)
                || this.oAuth2Properties.getGitea() != null && ThirdPartyTypeEnum.GITEA.name().equals(thirdPartyType)) {
            return;
        }
        throw new NotAllowThisPlatformLogInException("不允许" + thirdPartyType + "平台登录，请与管理员联系");
    }
}
