package dev.jianmu.infrastructure.worker.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class SecretVar
 * @description SecretVar
 * @create 2022/6/27 6:09 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecretVar {
    private String name;
    private String env;
}
