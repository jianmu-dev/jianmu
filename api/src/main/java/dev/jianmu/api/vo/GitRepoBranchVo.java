package dev.jianmu.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @class GitRepoBranchVo
 * @description GitRepoBranchVo
 * @author Daihw
 * @create 2022/7/8 2:25 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Git仓库分支Vo")
public class GitRepoBranchVo {
    private String branchName;
    private Boolean isDefault;
}
