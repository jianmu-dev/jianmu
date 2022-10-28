package dev.jianmu.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

/**
 * @class SpringbootApp
 * @description 项目启动类
 * @author Ethan Liu
 * @create 2021-02-12 15:35
*/
@SpringBootApplication(scanBasePackages = "dev.jianmu")
@MapperScan("dev.jianmu.infrastructure.mapper")
@EnableRetry
@EnableAsync(proxyTargetClass = true)
@OpenAPIDefinition(
        info = @Info(
                title = "建木持续集成平台",
                version = "2.0.0",
                description = "建木持续集成平台",
                license = @License(
                        name = "Mulan PSL v2",
                        url = "http://license.coscl.org.cn/MulanPSL2"
                )
        ),
        externalDocs = @ExternalDocumentation(
                description = "建木项目地址",
                url = "https://gitee.com/jianmu-dev"
        )
)
@ServletComponentScan
public class SpringbootApp implements WebMvcConfigurer {
    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        SpringApplication.run(SpringbootApp.class, args);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }
}
