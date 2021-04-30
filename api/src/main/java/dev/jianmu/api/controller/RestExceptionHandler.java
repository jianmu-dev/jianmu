package dev.jianmu.api.controller;

import dev.jianmu.api.vo.ErrorMessage;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.exception.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @class: RestExceptionHandler
 * @description: Rest全局异常处理类
 * @author: Ethan Liu
 * @create: 2021-04-06 20:40
 **/
@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

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
                .message("主键重复")
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage dataNotFoundException(DataNotFoundException ex, WebRequest request) {
        logger.error("数据异常: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(request.getDescription(false))
                .build();
    }

    @ExceptionHandler(DBException.DataNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage dbException(DBException.DataNotFound ex, WebRequest request) {
        logger.error("数据异常: ", ex);
        return ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
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
