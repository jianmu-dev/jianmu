package dev.jianmu.worker.aggregate;

/**
 * @class: DockerWorkerClient
 * @description: DockerWorkerClient接口
 * @author: Ethan Liu
 * @create: 2021-04-14 18:45
 **/
public class Worker {
    public enum Status {
        ONLINE,
        OFFLINE
    }

    public enum Type {
        EMBEDDED,
        DOCKER,
        SHELL
    }

    private String id;
    private String name;
    private Type type;
    private Status status;
}
