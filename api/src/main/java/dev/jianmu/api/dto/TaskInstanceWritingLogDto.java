package dev.jianmu.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Daihw
 * @class TaskInstanceWritingLogDto
 * @description TaskInstanceWritingLogDto
 * @create 2022/5/19 11:14 上午
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "写入任务日志Dto")
public class TaskInstanceWritingLogDto {
    static ObjectMapper objectMapper = new ObjectMapper();
    private Long number;
    private String content;
    private Long timestamp;

    public static List<TaskInstanceWritingLogDto> parseString(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析任务日志异常: " + e);
        }
    }
}
