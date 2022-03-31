package dev.jianmu.project.query;

import dev.jianmu.project.aggregate.Project;

import java.time.LocalDateTime;

public class ProjectVo extends Project {
    // 开始执行时间
    private LocalDateTime startTime;
    // 最后执行时间
    private LocalDateTime latestTime;
    // 最后执行状态
    private String status;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLatestTime() {
        return latestTime;
    }

    public String getStatus() {
        return status;
    }
}
