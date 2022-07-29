package dev.jianmu.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.dto.JwtResponse;
import dev.jianmu.api.dto.impl.GitlinkSilentLoggingDto;
import dev.jianmu.api.jwt.JwtProvider;
import dev.jianmu.api.jwt.JwtSession;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.application.exception.CodeExpiredException;
import dev.jianmu.application.exception.NotAllowRegistrationException;
import dev.jianmu.application.exception.NotAllowThisPlatformLogInException;
import dev.jianmu.application.exception.OAuth2IsNotConfiguredException;
import dev.jianmu.application.service.OAuth2Application;
import dev.jianmu.application.service.vo.AssociationData;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.JsonParseException;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
import dev.jianmu.oauth2.api.vo.ITokenVo;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import dev.jianmu.user.aggregate.User;
import dev.jianmu.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;

/**
 * @author huangxi
 * @class GitlinkController
 * @description GitlinkController
 * @create 2022-07-29 18:16
 */
@RestController
@RequestMapping
@Tag(name = "Gitlink控制器", description = "Gitlink控制器")
public class GitlinkController {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2Properties oAuth2Properties;
    private final OAuth2Application oAuth2Application;

    public GitlinkController(UserRepository userRepository, AuthenticationManager authenticationManager, JwtProvider jwtProvider, JwtProperties jwtProperties, OAuth2Properties oAuth2Properties, OAuth2Application oAuth2Application) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.oAuth2Properties = oAuth2Properties;
        this.oAuth2Application = oAuth2Application;
    }

    /**
     * 静默登录
     *
     * @param code
     * @return
     */
    @GetMapping("/auth/oauth2/login/silent")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestParam("code") String code) {
        ThirdPartyTypeEnum thirdPartyType = ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType());
        this.beforeAuthenticate();
        this.allowOrNotRegistration();
        this.allowThisPlatformLogIn(thirdPartyType.name());

        GitlinkSilentLoggingDto gitlinkSilentLoggingDto;
        try {
            String silentLoggingJson = AESEncryptionUtil.decryptWithIv(code, this.oAuth2Properties.getGitlink().getSilentLogin().getKey(), this.oAuth2Properties.getGitlink().getSilentLogin().getIv());
            gitlinkSilentLoggingDto = new ObjectMapper().readValue(silentLoggingJson, GitlinkSilentLoggingDto.class);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            throw new RuntimeException("code有误，请检查");
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e.getMessage());
        }

        if (new Date().getTime() > gitlinkSilentLoggingDto.getTimestamp() + this.oAuth2Properties.getGitlink().getSilentLogin().getCodeTimeout() * 1000L) {
            throw new CodeExpiredException("code已过期");
        }


        return ResponseEntity.ok(this.silentLogin(gitlinkSilentLoggingDto.getRef(), gitlinkSilentLoggingDto.getOwner(), gitlinkSilentLoggingDto.getUserId()));
    }

    public JwtResponse silentLogin(String ref, String owner, String userId) {
        ThirdPartyTypeEnum thirdPartyType = ThirdPartyTypeEnum.valueOf(this.oAuth2Properties.getThirdPartyType());
        OAuth2ApiProxy oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(thirdPartyType)
                .userId(userId)
                .build();

        ITokenVo tokenVo = oAuth2Api.getAccessToken();
        String accessToken = tokenVo.getAccessToken();
        String encryptedToken = tokenVo.getEncryptedAccessToken();
        long expireInMs = tokenVo.getExpireInMs();

        IUserInfoVo userInfoVo = oAuth2Api.getUserInfo(accessToken);

        var associationData = AssociationData.buildGitRepo(ref, owner);
        var association = this.oAuth2Application.getAssociation(thirdPartyType, accessToken, userInfoVo, associationData, userId);

        String userInfoId = userInfoVo.getId();
        Optional<User> userOptional = this.userRepository.findById(userInfoId);
        User user = User.Builder.aReference()
                .data(userInfoVo.getData())
                .id(userInfoId)
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

        return JwtResponse.builder()
                .type("Bearer")
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .entryUrl(oAuth2Api.getEntryUrl(owner, ref))
                .associationData(associationData)
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
