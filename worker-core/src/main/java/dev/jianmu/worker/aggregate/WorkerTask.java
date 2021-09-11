package dev.jianmu.worker.aggregate;

/**
 * @class: WorkerTask
 * @description: WorkerTask
 * @author: Ethan Liu
 * @create: 2021-09-10 22:17
 **/
public class WorkerTask {
    private String taskInstanceId;
    private String businessId;
    // 外部触发ID，流程实例唯一
    private String triggerId;
    // 任务定义唯一Key
    private String defKey;
    private String resultFile;
    // 容器规格定义
    private String spec;
}
