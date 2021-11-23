package dev.jianmu.application.exception;

/**
 * @class DataNotFoundException
 * @description 数据未找到异常
 * @author Ethan Liu
 * @create 2021-04-24 10:37
*/
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}
