package dev.jianmu.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Daihw
 * @class ProjectViewingDto
 * @description 项目查看Dto
 * @create 2021/11/24 4:07 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "项目查看Dto")
public class ProjectViewingDto extends PageDto {
    public enum SortType{
        DEFAULT_SORT,
        LAST_MODIFIED_TIME,
        LAST_EXECUTION_TIME
    }
    private String projectGroupId;
    private String name;
    private SortType sortType;

    public String getSortTypeName() {
        if (this.sortType == null) {
            return SortType.DEFAULT_SORT.name();
        }
        return this.sortType.name();
    }
}
