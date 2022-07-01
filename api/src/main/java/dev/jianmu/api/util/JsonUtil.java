package dev.jianmu.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author huangxi
 * @class JsonUtil
 * @description JsonUtil
 * @create 2021-06-30 14:08
 */
@Slf4j
public class JsonUtil {
    public static <T> String jsonToString(T t) {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.warn("Json序列化失败: {}", e.getMessage());
            throw new RuntimeException("Json序列化失败");
        }
    }

    public static <T> T stringToJson(String str, Class<T> cls) {
        var mapper = new ObjectMapper();
        try {
            return mapper.readValue(str, cls);
        } catch (JsonProcessingException e) {
            log.warn("Json反序列化失败: {}", e.getMessage());
            throw new RuntimeException("Json反序列化失败");
        }
    }
}
