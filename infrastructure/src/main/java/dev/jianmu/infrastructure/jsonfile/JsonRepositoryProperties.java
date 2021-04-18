package dev.jianmu.infrastructure.jsonfile;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @class: JsonRepositoryProperties
 * @description: Json文件仓储配置项
 * @author: Ethan Liu
 * @create: 2021-04-18 22:27
 **/
@Data
@Component
@ConfigurationProperties(prefix = "json.repository")
public class JsonRepositoryProperties {
    // 日志文件存储路径
    private String repoPath;
}
