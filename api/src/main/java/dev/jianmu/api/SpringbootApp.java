package dev.jianmu.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @class: SpringbootApp
 * @description: 项目启动类
 * @author: Ethan Liu
 * @create: 2021-02-12 15:35
 **/
@SpringBootApplication(scanBasePackages = "dev.jianmu")
@MapperScan("dev.jianmu.infrastructure.mapper")
@EnableRetry
@EnableAsync(proxyTargetClass = true)
@EnableAspectJAutoProxy(exposeProxy = true)
@OpenAPIDefinition(
        info = @Info(
                title = "建木自动化集成平台",
                version = "2.0",
                description = "建木自动化集成平台",
                license = @License(
                        name = "Apache-2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "建木项目地址",
                url = "https://gitee.com/jianmu_dev"
        )
)
public class SpringbootApp {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApp.class, args);
    }
}
