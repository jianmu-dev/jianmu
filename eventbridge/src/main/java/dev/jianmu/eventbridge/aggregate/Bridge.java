package dev.jianmu.eventbridge.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @class: Bridge
 * @description: Bridge
 * @author: Ethan Liu
 * @create: 2021-09-24 15:37
 **/
public class Bridge {
    private String id;
    private String name;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
    // 最后修改者
    private String lastModifiedBy;
    // 最后修改时间
    private LocalDateTime lastModifiedTime;

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public void setLastModifiedTime() {
        this.lastModifiedTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public static final class Builder {
        private String name;
        // 最后修改者
        private String lastModifiedBy;

        private Builder() {
        }

        public static Builder aBridge() {
            return new Builder();
        }

        public Builder name(String name) {
            if (name == null)
                throw new RuntimeException("name不能为空");
            this.name = name;
            return this;
        }

        public Builder lastModifiedBy(String lastModifiedBy) {
            this.lastModifiedBy = lastModifiedBy;
            return this;
        }

        public Bridge build() {
            Bridge bridge = new Bridge();
            bridge.id = UUID.randomUUID().toString().replace("-", "");
            bridge.name = this.name;
            bridge.lastModifiedBy = this.lastModifiedBy;
            bridge.lastModifiedTime = LocalDateTime.now();
            return bridge;
        }
    }
}
