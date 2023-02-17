package dev.jianmu.api.controller;

import dev.jianmu.api.vo.ClientSessionVo;
import dev.jianmu.api.vo.ErrorMessage;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.exception.NoAssociatedPermissionException;
import dev.jianmu.application.service.GitRepoApplication;
import dev.jianmu.application.util.AssociationUtil;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.jianmu_user_context.holder.UserSessionHolder;
import dev.jianmu.oauth2.api.config.OAuth2Properties;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.vault.VaultException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @author Ethan Liu
 * @class RestExceptionHandler
 * @description Rest全局异常处理类
 * @create 2021-04-06 20:40
 */
@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    private final RestTemplate restTemplate;
    private final GitRepoApplication gitRepoApplication;
    private final UserSessionHolder userSessionHolder;
    private final OAuth2Properties oAuth2Properties;

    public RestExceptionHandler(
            RestTemplate restTemplate,
            GitRepoApplication gitRepoApplication,
            UserSessionHolder userSessionHolder,
            OAuth2Properties oAuth2Properties
    ) {
        this.restTemplate = restTemplate;
        this.gitRepoApplication = gitRepoApplication;
        this.userSessionHolder = userSessionHolder;
        this.oAuth2Properties = oAuth2Properties;
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
                .message(ex.getCause().getMessage().contains("jm_project.association_id_type_platform_workflow_name") ? "项目名称不能重复" : "主键重复")
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

    @ExceptionHandler(ClientAbortException.class)
    public void clientAbortException(Exception ex, HandlerMethod handlerMethod, WebRequest request) {
        logger.error("client abort: class:{} params:{}", handlerMethod.getBeanType(), handlerMethod.getMethodParameters());
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

    @ExceptionHandler(NoAssociatedPermissionException.class)
    public ResponseEntity<?> validationNoAssociatedPermissionException(NoAssociatedPermissionException ex, WebRequest request) {
        if (!AssociationUtil.AssociationType.GIT_REPO.name().equals(ex.getAssociationType())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorMessage.builder()
                            .statusCode(HttpStatus.BAD_REQUEST.value())
                            .timestamp(LocalDateTime.now())
                            .message("无权限")
                            .description(request.getDescription(false))
                            .build());
        }
        var session = this.userSessionHolder.getSession();
        var gitRepo = this.gitRepoApplication.findById(ex.getAssociationId());
        try {
            var url = this.oAuth2Properties.getGitlink().getEngineAddress() + "gitlink_engine/auth/session";
            var headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, session.getId());
            var param = new HashMap<String, Object>();
            param.put("ref", gitRepo.getRef());
            param.put("owner", gitRepo.getOwner());
            var sessionVo = this.restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(param, headers), ClientSessionVo.class).getBody();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(sessionVo);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}
