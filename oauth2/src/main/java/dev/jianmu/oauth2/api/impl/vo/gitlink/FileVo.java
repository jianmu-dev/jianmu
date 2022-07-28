package dev.jianmu.oauth2.api.impl.vo.gitlink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jianmu.oauth2.api.vo.IFileVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author huangxi
 * @class FileVo
 * @description FileVo
 * @create 2022-07-27 15:51
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileVo implements IFileVo {
    private Entries entries;
    private Integer status;
    private String message;

    @Override
    public String getName() {
        return this.entries.getName();
    }

    @Override
    public String getContent() {
        return this.entries.getContent();
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entries {
        private String name;
        private String content;
    }
}
