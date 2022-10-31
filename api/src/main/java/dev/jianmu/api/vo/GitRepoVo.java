package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @class GitRepoVo
 * @description GitRepoVo
 * @author Daihw
 * @create 2022/10/28 11:19 上午
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepoVo {
    @Schema(required = true)
    private String ref;
    @Schema(required = true)
    private String owner;
}
