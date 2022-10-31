package dev.jianmu.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.api.vo.SessionVo;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Base64;
import java.util.Objects;

@Component
public class UserContextHolder {
    private final static ObjectMapper MAPPER = new ObjectMapper();

    static {
        // 忽略不存在的属性
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 获取Session
     *
     * @return
     */
    public SessionVo getSession() {
        var header = (((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                .getRequest())
                .getHeader("X-Session");
        var decode = new String(Base64.getDecoder().decode(header));
        try {
            return MAPPER.readValue(decode, SessionVo.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("获取Session失败", e);
        }
    }
}