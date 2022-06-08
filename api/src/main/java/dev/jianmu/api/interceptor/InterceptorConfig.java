package dev.jianmu.api.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Daihw
 * @class InterceptorConfig
 * @description InterceptorConfig
 * @create 2022/5/26 3:49 下午
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    private final WorkerInterceptor workerInterceptor;

    public InterceptorConfig(WorkerInterceptor workerInterceptor) {
        this.workerInterceptor = workerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.workerInterceptor).addPathPatterns("/workers/**");
    }
}
