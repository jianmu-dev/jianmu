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
    // 日志文件存储路径
    private String logfilePath;
    // webhook存储路径
    private String webhookFilePath;
}
