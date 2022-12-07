package dev.jianmu.oauth2.api.exception;

/**
 * @class RoleNotExistedException
 * @description RoleNotExistedException
 * @author Daihw
 * @create 2022/12/6 3:56 下午
 */
public class RoleNotExistedException extends RuntimeException {
    public RoleNotExistedException(String message) {
        super(message);
    }
}
