package dev.jianmu.infrastructure.jsonfile;

/**
 * @class: RollbackEvent
 * @description: 文件事务回滚处理事件
 * @author: Ethan Liu
 * @create: 2021-04-20 18:58
 **/
public class RollbackEvent {
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
