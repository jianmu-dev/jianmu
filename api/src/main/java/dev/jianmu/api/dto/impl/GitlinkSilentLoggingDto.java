package dev.jianmu.api.dto.impl;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author huangxi
 * @class GitlinkSilentLoggingDto
 * @description GitlinkSilentLoggingDto
 * @create 2022-07-28 14:06
 */
@Getter
@Setter
public class GitlinkSilentLoggingDto {
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

    /**
     * userId
     */
    @NotBlank(message = "userId不能为空")
    private String userId;

    /**
     * 时间戳
     */
    @NotNull(message = "时间戳不能为空")
    private Long timestamp;


}
