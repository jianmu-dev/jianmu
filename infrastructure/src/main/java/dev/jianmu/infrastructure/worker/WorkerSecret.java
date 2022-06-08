package dev.jianmu.infrastructure.worker;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @class WorkerSecret
 * @description WorkerSecret
 * @author Daihw
 * @create 2022/6/8 11:53 上午
 */
@Getter
@Setter
@Builder
public class WorkerSecret {
    private String env;
    private String data;
    private Boolean mask;
}
