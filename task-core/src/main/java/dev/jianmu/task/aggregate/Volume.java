package dev.jianmu.task.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-16 21:59
 */
public class Volume {
    public Volume() {
    }

    public enum Scope {
        WORKFLOW,
        PROJECT,
    }

    // ID
    private String id;
    // 名称
    private String name;
    // 作用域
    private Scope scope;
    // 关联项目ref
    private String workflowRef;
    // workerId
    private String workerId;
    // 是否可用
    private boolean available = false;
    // 是否删除失败
    private boolean taint = false;
    // 是否正在清理
    private boolean cleaning = false;
    // 创建时间
    private final LocalDateTime createdTime = LocalDateTime.now();
    // 可用时间
    private LocalDateTime availableTime;

    public void activate(String workerId) {
        this.workerId = workerId;
        this.available = true;
        this.cleaning = false;
        this.availableTime = LocalDateTime.now();
    }

    public void taint() {
        this.taint = true;
    }

    public void clean() {
        this.cleaning = true;
        this.available = false;
    }

    public String getMountName() {
        if (this.workflowRef == null) {
            return this.name;
        }
        return this.workflowRef + "_" + this.name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Scope getScope() {
        return scope;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkerId() {
        return workerId;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isTainted() {
        return taint;
    }

    public boolean isCleaning() {
        return cleaning;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getAvailableTime() {
        return availableTime;
    }

    public static final class Builder {
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        private String name;
        private Scope scope;
        private String workflowRef;

        private Builder() {
        }

        public static Builder aVolume() {
            return new Builder();
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Volume build() {
            Volume volume = new Volume();
            volume.name = this.name;
            volume.id = this.id;
            volume.scope = this.scope;
            volume.workflowRef = this.workflowRef;
            return volume;
        }
    }
}
