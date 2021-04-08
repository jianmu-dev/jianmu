package dev.jianmu.infrastructure.storage;

/**
 * @class: StorageFileNotFoundException
 * @description: 文件未找到异常
 * @author: Ethan Liu
 * @create: 2021-04-05 21:05
 **/
public class StorageFileNotFoundException extends StorageException {
    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
