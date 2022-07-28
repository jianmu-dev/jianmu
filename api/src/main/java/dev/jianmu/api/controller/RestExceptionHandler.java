package dev.jianmu.api.controller;

import dev.jianmu.api.dto.JwtResponse;
import dev.jianmu.api.jwt.JwtProvider;
import dev.jianmu.api.jwt.JwtSession;
import dev.jianmu.api.jwt.UserContextHolder;
import dev.jianmu.api.util.JsonUtil;
import dev.jianmu.api.vo.ErrorMessage;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.NoAssociatedPermissionException;
import dev.jianmu.application.exception.OAuth2EntryException;
import dev.jianmu.application.exception.OAuth2IsNotAuthorizedException;
import dev.jianmu.application.service.OAuth2Application;
import dev.jianmu.application.service.vo.AssociationData;
import dev.jianmu.git.repo.aggregate.GitRepo;
import dev.jianmu.git.repo.repository.GitRepoRepository;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import dev.jianmu.oauth2.api.enumeration.RoleEnum;
import dev.jianmu.oauth2.api.enumeration.ThirdPartyTypeEnum;
import dev.jianmu.oauth2.api.exception.RepoExistedException;
import dev.jianmu.oauth2.api.impl.OAuth2ApiProxy;
import dev.jianmu.oauth2.api.util.AESEncryptionUtil;
import dev.jianmu.oauth2.api.vo.IUserInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.vault.VaultException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class RestExceptionHandler
 * @description Rest全局异常处理类
 * @create 2021-04-06 20:40
 */
@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final UserContextHolder userContextHolder;
    private final OAuth2Properties oAuth2Properties;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2Application oAuth2Application;
    private final GitRepoRepository gitRepoRepository;

    public RestExceptionHandler(UserContextHolder userContextHolder, OAuth2Properties oAuth2Properties, AuthenticationManager authenticationManager, JwtProvider jwtProvider, JwtProperties jwtProperties, OAuth2Application oAuth2Application, GitRepoRepository gitRepoRepository) {
        this.userContextHolder = userContextHolder;
        this.oAuth2Properties = oAuth2Properties;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.jwtProperties = jwtProperties;
        this.oAuth2Application = oAuth2Application;
        this.gitRepoRepository = gitRepoRepository;
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage validationBodyException(BindException ex, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message(fieldErrors.get(0).getDefaultMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(NoAssociatedPermissionException.class)
    public ResponseEntity<?> validationNoAssociatedPermissionException(NoAssociatedPermissionException ex, WebRequest request) {
        JwtSession session = this.userContextHolder.getSession();

        String thirdPartyType = this.oAuth2Properties.getThirdPartyType();
        OAuth2ApiProxy oAuth2Api = OAuth2ApiProxy.builder()
                .thirdPartyType(ThirdPartyTypeEnum.valueOf(thirdPartyType))
                .build();

        IUserInfoVo userInfo;
        String accessToken;
        String encryptedToken;
        RoleEnum role;
        GitRepo gitRepo;
        AssociationData associationData;
        try {
            encryptedToken = session.getEncryptedToken();
            accessToken = AESEncryptionUtil.decrypt(encryptedToken, this.oAuth2Properties.getClientSecret());
            userInfo = oAuth2Api.getUserInfo(accessToken);
            Optional<GitRepo> gitRepoOptional = this.gitRepoRepository.findById(ex.getAssociationId());
            if (gitRepoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            gitRepo = gitRepoOptional.get();
            associationData = AssociationData.buildGitRepo(gitRepo.getRef(), gitRepo.getOwner());
            role = this.oAuth2Application.getAssociation(ThirdPartyTypeEnum.valueOf(thirdPartyType), accessToken, userInfo,
                    associationData).getRole();
        } catch (OAuth2IsNotAuthorizedException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (OAuth2EntryException | RepoExistedException e) {
            logger.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }

        long expireTimestamp = session.getExpireTimestamp();
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(JsonUtil.jsonToString(JwtSession.builder()
                        .avatarUrl(userInfo.getAvatarUrl())
                        .id(userInfo.getId())
                        .username(userInfo.getUsername())
                        .nickname(userInfo.getNickname())
                        .role(role)
                        .associationId(ex.getAssociationId())
                        .associationType(ex.getAssociationType())
                        .encryptedToken(encryptedToken)
                        .expireTimestamp(expireTimestamp)
                        .build()),
                        this.jwtProperties.getPassword(this.oAuth2Properties.getClientSecret())));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String newJwt = this.jwtProvider.generateJwtToken(authentication, expireTimestamp);

        return ResponseEntity.status(HttpStatus.CREATED).body(JwtResponse.builder()
                .type("Bearer")
                .token(newJwt)
                .id(session.getId())
                .username(session.getUsername())
                .avatarUrl(session.getAvatarUrl())
                .thirdPartyType(this.oAuth2Properties.getThirdPartyType())
                .entryUrl(oAuth2Api.getEntryUrl(gitRepo.getOwner(), gitRepo.getRef()))
                .associationData(associationData)
                .build());
    }

    @ExceptionHandler({BadSqlGrammarException.class, SQLException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage sqlException(Exception ex, WebRequest request) {
        logger.error("Sql异常: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message("Sql执行错误")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler({SQLIntegrityConstraintViolationException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage sqlIntegrityConstraintViolationException(Exception ex, WebRequest request) {
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message("数据完整性错误")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler({DuplicateKeyException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage duplicateKeyException(DuplicateKeyException ex, WebRequest request) {
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getCause().getMessage().contains("id_type_workflow_name_UNIQUE") ? "项目名称不能重复" : "主键重复")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(VaultException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage vaultException(VaultException ex, WebRequest request) {
        logger.warn("Vault异常: {}", ex.getMessage());
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage dataNotFoundException(DataNotFoundException ex, WebRequest request) {
        logger.error("数据异常: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(DBException.DataNotFound.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage dbException(DBException.DataNotFound ex, WebRequest request) {
        logger.error("数据异常: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage runtimeException(RuntimeException ex, WebRequest request) {
        logger.error("Got ex: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(OAuth2IsNotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public JwtResponse handleOAuth2IsNotAuthorizedException(OAuth2IsNotAuthorizedException ex) {
        logger.error("Got ex: ", ex);
        return JwtResponse.builder()
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler({OAuth2EntryException.class, RepoExistedException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleOAuth2EntryException(Exception ex) {
        logger.error("Got ex: ", ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage globalExceptionHandler(Exception ex, WebRequest request) {
        logger.error("Got ex: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }
}
