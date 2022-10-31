package dev.jianmu.api.dto;

import dev.jianmu.git.repo.aggregate.Branch;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @class GitRepoSyncingDto
 * @description GitRepoSyncingDto
 * @author Daihw
 * @create 2022/10/26 3:01 下午
 */
@Getter
public class GitRepoSyncingDto {
    @NotBlank(message = "仓库ID不能为空")
    private String id;
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    @NotBlank(message = "仓库归属表示标识不能为空")
    private String ownerRef;
    @NotBlank(message = "仓库标识不能为空")
    private String ref;
    @NotNull(message = "分支不能为空")
    private List<Branch> branches;
}
