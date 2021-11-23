package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.WorkflowVo;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @class WorkflowMapper
 * @description WorkflowMapper
 * @author Ethan Liu
 * @create 2021-10-31 14:42
*/
@Mapper
public interface WorkflowMapper {
    WorkflowMapper INSTANCE = Mappers.getMapper(WorkflowMapper.class);

    WorkflowVo toWorkflowVo(Workflow workflow);
}
