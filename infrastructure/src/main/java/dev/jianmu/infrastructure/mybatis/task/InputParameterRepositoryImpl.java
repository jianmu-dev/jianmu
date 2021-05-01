package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.InputParameterMapper;
import dev.jianmu.task.aggregate.InputParameter;
import dev.jianmu.task.repository.InputParameterRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @class: InputParameterRepositoryImpl
 * @description: 任务定义输入覆盖参数仓储实现
 * @author: Ethan Liu
 * @create: 2021-04-28 21:40
 **/
@Repository
public class InputParameterRepositoryImpl implements InputParameterRepository {
    private final InputParameterMapper inputParameterMapper;

    public InputParameterRepositoryImpl(InputParameterMapper inputParameterMapper) {
        this.inputParameterMapper = inputParameterMapper;
    }

    @Override
    public void addAll(List<InputParameter> inputParameters) {
        if (!inputParameters.isEmpty()) {
            this.inputParameterMapper.addAll(inputParameters);
        }
    }

    @Override
    public void deleteByProjectId(String projectId) {
        this.inputParameterMapper.deleteByProjectId(projectId);
    }

    @Override
    public List<InputParameter> findByWorkflowRefAndWorkflowVersionAndAsyncTaskRef(String workflowRef, String workflowVersion, String asyncTaskRef) {
        return this.inputParameterMapper.findByWorkflowRefAndWorkflowVersionAndAsyncTaskRef(workflowRef, workflowVersion, asyncTaskRef);
    }
}
