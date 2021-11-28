package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.WorkflowInstanceVo;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @class WorkflowInstanceMapper
 * @description 流程实例Mapper
 * @author Ethan Liu
 * @create 2021-04-22 11:16
*/
@Mapper(imports = {AsyncTaskInstance.class, TaskStatus.class})
public interface WorkflowInstanceMapper {
    WorkflowInstanceMapper INSTANCE = Mappers.getMapper(WorkflowInstanceMapper.class);

    WorkflowInstanceVo toWorkflowInstanceVo(WorkflowInstance workflowInstance);

    List<WorkflowInstanceVo> toWorkflowInstanceVoList(List<WorkflowInstance> workflowInstanceList);
}
