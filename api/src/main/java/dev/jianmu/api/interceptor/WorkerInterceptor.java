package dev.jianmu.api.interceptor;

import dev.jianmu.infrastructure.worker.WorkerProperties;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @class WorkerInterceptor
 * @description WorkerInterceptor
 * @author Daihw
 * @create 2022/5/26 3:49 下午
 */
@Component
public class WorkerInterceptor implements HandlerInterceptor {
    private final WorkerProperties workerProperties;

    public WorkerInterceptor(WorkerProperties workerProperties) {
        this.workerProperties = workerProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("X-Jianmu-Token");
        if (StringUtils.hasText(token) && token.equals(this.workerProperties.getSecret())) {
            return true;
        }
        response.setStatus(HttpStatus.SC_FORBIDDEN);
        return false;
    }
}
