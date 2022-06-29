package dev.jianmu.infrastructure.worker.unit;

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
    private PodSpec podSpec;
    private Volume volume;
    private List<WorkerSecret> secrets;
    private WorkerSecret pullSecret;
    private List<Runner> internal;
    private List<Runner> runners;

    public enum Type {
        RUN,
        CREATE,
        DELETE
    }
}
