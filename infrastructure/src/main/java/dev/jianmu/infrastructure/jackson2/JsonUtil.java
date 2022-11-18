package dev.jianmu.infrastructure.jackson2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Daihw
 * @class JsonUtil
 * @description JsonUtil
 * @create 2022/11/18 11:45 上午
 */
@Slf4j
public class JsonUtil {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String jsonToString(T t) {
        try {
            return MAPPER.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.warn("Json序列化失败: {}", e.getMessage());
            throw new RuntimeException("Json序列化失败");
        }
    }

    public static <T> T stringToJson(String str, Class<T> cls) {
        try {
            return MAPPER.readValue(str, cls);
        } catch (JsonProcessingException e) {
            log.warn("Json反序列化失败: {}", e.getMessage());
            throw new RuntimeException("Json反序列化失败");
        }
    }
}
