package dev.jianmu.infrastructure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author Ethan Liu
 * @class GlobalProperties
 * @description 全局配置
 * @create 2022-01-04 08:48
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "jianmu")
public class GlobalProperties {
    @NotNull
    private Global global = new Global();

    @Data
    @Component
    @Validated
    public class Global {
        @NotNull
        private Record record = new Record();

        @Data
        @Component
        @Validated
        public class Record {
            @NotNull
            private Long max = 9999L;
            @NotNull
            private Boolean autoClean = false;
        }
    }
}
