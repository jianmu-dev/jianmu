package dev.jianmu.application.exception;

/**
 * @class ProjectGroupException
 * @description 项目组异常
 * @author Daihw
 * @create 2021/11/30 10:33 上午
 */
public class ProjectGroupException extends RuntimeException {
    public ProjectGroupException(String message) {
        super(message);
    }
}
