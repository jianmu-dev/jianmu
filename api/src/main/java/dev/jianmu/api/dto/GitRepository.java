package dev.jianmu.api.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @class GitRepository
 * @description GitRepository
 * @author Daihw
 * @create 2022/7/29 4:11 下午
 */
@Data
public class GitRepository {
    @NotBlank(message = "repository.name不能为空")
    private String name;
    @Valid
    @NotNull(message = "repository.owner不能为空")
    private GitOwner owner;
}
