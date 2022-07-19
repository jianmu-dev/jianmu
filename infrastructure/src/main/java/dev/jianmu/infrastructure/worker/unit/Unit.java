package dev.jianmu.infrastructure.worker.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.jianmu.infrastructure.worker.WorkerSecret;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Daihw
 * @class Unit
 * @description Unit
 * @create 2022/6/27 5:32 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Unit {
    private Type type;
    @JsonProperty("pod_spec")
    private PodSpec podSpec;
    private List<Volume> volumes;
    private List<WorkerSecret> secrets;
    @JsonProperty("pull_secrets")
    private WorkerSecret pullSecret;
    private Runner current;
    private List<Runner> runners;

    public enum Type {
        RUN,
        CREATE,
        DELETE
    }
}
