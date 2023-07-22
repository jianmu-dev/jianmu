package dev.jianmu.api.interceptor;

import dev.jianmu.infrastructure.GlobalProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @class WorkerInterceptor
 * @description WorkerInterceptor
 * @author Daihw
 * @create 2022/5/26 3:49 下午
 */
@Component
public class WorkerInterceptor implements HandlerInterceptor {
    private final GlobalProperties globalProperties;

    public WorkerInterceptor(GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("X-Jianmu-Token");
        if (StringUtils.hasText(token) && token.equals(this.globalProperties.getWorker().getSecret())) {
            return true;
        }
        response.setStatus(HttpStatus.SC_FORBIDDEN);
        return false;
    }
}
