package dev.jianmu.api.dto;

import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class GitRepoFlowViewingDto
 * @description GitRepoFlowViewingDto
 * @create 2022/7/5 5:18 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "查询Git仓库流水线Dto")
public class GitRepoFlowViewingDto {
    private String name;
    private ProcessStatus status;
    private String branch;
    private ProjectViewingDto.SortType sortType;

    @Schema(hidden = true)
    public String getSortTypeName() {
        if (this.sortType == null) {
            return SortType.LAST_EXECUTION_TIME.name();
        }
        return this.sortType.name();
    }

    private enum SortType {
        LAST_MODIFIED_TIME,
        LAST_EXECUTION_TIME
    }
}
