package dev.jianmu.infrastructure.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @class StorageProperties
 * @description 存储配置项
 * @author Ethan Liu
 * @create 2021-04-05 20:41
*/
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    // 文件路径
    private String filepath = "ci";
    // SSE超时时间，毫秒值
    private Long sseTimeout = 1000L * 60;
}
