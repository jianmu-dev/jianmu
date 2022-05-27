package dev.jianmu.infrastructure.worker;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * @class WorkerProperties
 * @description Worker配置
 * @author Daihw
 * @create 2022/5/23 10:00 上午
 */
@Validated
@Data
@Component
@ConfigurationProperties(prefix = "worker")
public class WorkerProperties {
    // Worker Secret
    @NotEmpty(message = "worker必须配置secret")
    private String secret;
}
