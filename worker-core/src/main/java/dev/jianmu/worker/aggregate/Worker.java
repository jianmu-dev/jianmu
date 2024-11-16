package dev.jianmu.worker.aggregate;

import java.time.LocalDateTime;

/**
 * @class DockerWorkerClient
 * @description DockerWorkerClient接口
 * @author Ethan Liu
 * @create 2021-04-14 18:45
*/
public class Worker {
    public enum Status {
        ONLINE,
        OFFLINE
    }

    public enum Type {
        EMBEDDED,
        DOCKER,
        KUBERNETES,
        SHELL
    }

    private String id;
    private String name;
    private String tags;
    private Integer capacity;
    private String os;
    private String arch;
    private Type type;
    private Status status;
    private final LocalDateTime createdTime = LocalDateTime.now();

    public void online() {
        this.status = Status.ONLINE;
    }

    public void offline() {
        this.status = Status.OFFLINE;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTags() {
        return tags;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getOs() {
        return os;
    }

    public String getArch() {
        return arch;
    }

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public static final class Builder {
        private String id;
        private String name;
        private String tags;
        private Integer capacity;
        private String os;
        private String arch;
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

        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }

        public Builder capacity(Integer capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder os(String os) {
            this.os = os;
            return this;
        }

        public Builder arch(String arch) {
            this.arch = arch;
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
            worker.name = this.name;
            worker.tags = this.tags;
            worker.capacity = this.capacity;
            worker.os = this.os;
            worker.arch = this.arch;
            worker.type = this.type;
            worker.status = this.status;
            return worker;
        }
    }
}
