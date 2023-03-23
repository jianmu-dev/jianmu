package dev.jianmu.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @class WorkflowInstanceViewingIdsDto
 * @description WorkflowInstanceViewingIdsDto
 * @author Daihw
 * @create 2023/3/23 2:05 下午
 */
@Getter
@Setter
public class WorkflowInstanceViewingIdsDto {
    @NotNull(message = "id不能为空")
    private List<String> ids;
}
