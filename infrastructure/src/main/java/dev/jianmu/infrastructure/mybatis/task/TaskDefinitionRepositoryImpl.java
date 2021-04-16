package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.TaskDefinitionMapper;
import dev.jianmu.infrastructure.mapper.task.TaskDefinitionParameterMapper;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @class: DefinitionRepositoryImpl
 * @description: 任务定义仓储实现类
 * @author: Ethan Liu
 * @create: 2021-03-25 21:00
 **/
//@Repository
public class TaskDefinitionRepositoryImpl implements TaskDefinitionRepository {

    private final TaskDefinitionMapper taskDefinitionMapper;
    private final TaskDefinitionParameterMapper taskDefinitionParameterMapper;

    @Inject
    public TaskDefinitionRepositoryImpl(
            TaskDefinitionMapper taskDefinitionMapper,
            TaskDefinitionParameterMapper taskDefinitionParameterMapper) {
        this.taskDefinitionMapper = taskDefinitionMapper;
        this.taskDefinitionParameterMapper = taskDefinitionParameterMapper;
    }

    @Override
    public void add(TaskDefinition taskDefinition) {
        this.taskDefinitionMapper.add(taskDefinition);
        if (taskDefinition.getParameters().size() > 0) {
            this.taskDefinitionParameterMapper.addAll(
                    taskDefinition.getKey() + taskDefinition.getVersion(),
                    taskDefinition.getParameters()
            );
        }
    }

    @Override
    public Optional<TaskDefinition> findByKeyVersion(String keyVersion) {
        var definitionOptional = this.taskDefinitionMapper.findByKeyVersion(keyVersion);
        var defParameters = this.taskDefinitionParameterMapper.findByTaskDefinitionId(keyVersion);
        definitionOptional.ifPresent(taskDefinition -> {
            taskDefinition.setParameters(defParameters);
        });
        return definitionOptional;
    }

    @Override
    public List<TaskDefinition> findAll() {
        return this.taskDefinitionMapper.findAll();
    }
}
