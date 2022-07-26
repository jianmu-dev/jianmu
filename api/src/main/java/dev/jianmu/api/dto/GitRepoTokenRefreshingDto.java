package dev.jianmu.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author huangxi
 * @class GitRepoTokenRefreshingDto
 * @description GitRepoTokenRefreshingDto
 * @create 2022-07-26 14:54
 */
@Getter
@Setter
public class GitRepoTokenRefreshingDto {
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
