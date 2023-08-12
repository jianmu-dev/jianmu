package dev.jianmu.api;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;

/**
 * @author Ethan Liu
 * @class SpringbootApp
 * @description 项目启动类
 * @create 2021-02-12 15:35
 */
@SpringBootApplication(scanBasePackages = "dev.jianmu")
@MapperScan("dev.jianmu.infrastructure.mapper")
@EnableRetry
@EnableAsync(proxyTargetClass = true)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
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
public class SpringbootApp implements WebMvcConfigurer, WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApp.class, args);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

    // TODO: 此配置有安全隐患，未来产品需要考虑
    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        TomcatConnectorCustomizer tomcatConnectorCustomizer =
                connector -> connector.setEncodedSolidusHandling(EncodedSolidusHandling.DECODE.getValue());
        factory.setTomcatConnectorCustomizers(List.of(tomcatConnectorCustomizer));
    }
}
