package dev.jianmu.api.dto.impl;

import dev.jianmu.api.dto.Oauth2LoggingDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author huangxi
 * @class GitRepoLoggingDto
 * @description GitRepoLoggingDto
 * @create 2022-07-22 17:27
 */
@Getter
@Setter
public class GitRepoLoggingDto extends Oauth2LoggingDto {
    /**
     * 仓库名
     */
    @NotBlank(message = "仓库唯一标识不能为空")
    private String ref;

    /**
     * 仓库所有者
     */
    @NotBlank(message = "仓库所有者不能为空")
    private String owner;
}
