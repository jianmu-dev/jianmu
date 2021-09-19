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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public static final class Builder {
        private String id;
        private String name;
        private Type type;
        private Status status;

        private Builder() {
        }

        public static Builder aWorker() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Worker build() {
            Worker worker = new Worker();
            worker.id = this.id;
            worker.status = this.status;
            worker.name = this.name;
            worker.type = this.type;
            return worker;
        }
    }
}
