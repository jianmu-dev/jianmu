package dev.jianmu.infrastructure.storage;

/**
 * @class: StorageException
 * @description: 存储异常定义
 * @author: Ethan Liu
 * @create: 2021-04-05 21:04
 **/
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
