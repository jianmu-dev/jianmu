package dev.jianmu.task.aggregate;

import java.util.Map;

/**
 * @class: Worker
 * @description: 任务执行器
 * @author: Ethan Liu
 * @create: 2021-03-26 16:28
 **/
public class Worker {
    public enum Status {
        ONLINE,
        OFFLINE
    }
    public enum Type {
        DOCKER,
        SHELL
    }
    private String id;
    private String name;
    private Status status;
    private Type type;

    private Worker() {
    }

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

    public Status getStatus() {
        return status;
    }

    public Type getType() {
        return type;
    }

    public static final class Builder {
        private String id;
        private String name;
        private Status status;
        private Type type;

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

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Worker build() {
            Worker worker = new Worker();
            worker.id = this.id;
            worker.type = this.type;
            worker.status = this.status;
            worker.name = this.name;
            return worker;
        }
    }
}
