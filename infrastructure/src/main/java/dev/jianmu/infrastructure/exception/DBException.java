package dev.jianmu.infrastructure.exception;

/**
 * @class: DBException
 * @description: 数据库异常定义
 * @author: Ethan Liu
 * @create: 2021-03-22 13:07
 **/
public class DBException {
    public static class OptimisticLocking extends RuntimeException {
        public OptimisticLocking(String message) {
            super(message);
        }
    }

    public static class InsertFailed extends RuntimeException {
        public InsertFailed(String message) {
            super(message);
        }
    }
}
