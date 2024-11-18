package dev.jianmu.api.vo;

import dev.jianmu.worker.aggregate.Worker;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Worker定义VO")
public class WorkerVo {
    private String id;
    private String name;
    private String tags;
    private Integer capacity;
    private String os;
    private String arch;
    private Worker.Type type;
    private Worker.Status status;
    private LocalDateTime createdTime;
}
