package dev.jianmu.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Ethan Liu
 * @class GlobalProperties
 * @description 全局配置
 * @create 2022-01-04 08:48
 */
@Data
@Component
@ConfigurationProperties(prefix = "jianmu.global")
public class GlobalProperties {
    private Long latestRecords = 50L;
    private Boolean autoClean = false;
}
