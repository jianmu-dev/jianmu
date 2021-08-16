package dev.jianmu.eventbridge.aggregate;

import java.util.UUID;

/**
 * @class: Source
 * @description: 事件来源
 * @author: Ethan Liu
 * @create: 2021-08-10 11:40
 **/
public class Source {
    public enum Type {
        WEBHOOK,
        SERVICE
    }

    private String id;
    private String name;
    private Type type;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    // TODO 临时测试代码，需要删除
    public void setId(String id) {
        this.id = id;
    }

    public static final class Builder {
        private String name;
        private Type type;

        private Builder() {
        }

        public static Builder aSource() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Source build() {
            Source source = new Source();
            source.id = UUID.randomUUID().toString().replace("-", "");
            source.name = this.name;
            source.type = this.type;
            return source;
        }
    }
}
