package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.TaskDefinitionMapper;
import dev.jianmu.infrastructure.mapper.task.TaskDefinitionParameterMapper;
import dev.jianmu.infrastructure.mapper.task.TaskDefinitionWorkerParameterMapper;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @class: DefinitionRepositoryImpl
 * @description: 任务定义仓储实现类
 * @author: Ethan Liu
 * @create: 2021-03-25 21:00
 **/
@Repository
public class TaskDefinitionRepositoryImpl implements TaskDefinitionRepository {

    private final TaskDefinitionMapper taskDefinitionMapper;
    private final TaskDefinitionParameterMapper taskDefinitionParameterMapper;
    private final TaskDefinitionWorkerParameterMapper taskDefinitionWorkerParameterMapper;

    @Inject
    public TaskDefinitionRepositoryImpl(
            TaskDefinitionMapper taskDefinitionMapper,
            TaskDefinitionParameterMapper taskDefinitionParameterMapper,
            TaskDefinitionWorkerParameterMapper taskDefinitionWorkerParameterMapper) {
        this.taskDefinitionMapper = taskDefinitionMapper;
        this.taskDefinitionParameterMapper = taskDefinitionParameterMapper;
        this.taskDefinitionWorkerParameterMapper = taskDefinitionWorkerParameterMapper;
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
        if (taskDefinition.getWorkerParameters().size() > 0) {
            this.taskDefinitionWorkerParameterMapper.addAll(
                    taskDefinition.getKey() + taskDefinition.getVersion(),
                    taskDefinition.getWorkerParameters()
            );
        }
    }

    @Override
    public Optional<TaskDefinition> findByKeyVersion(String keyVersion) {
        var definitionOptional = this.taskDefinitionMapper.findByKeyVersion(keyVersion);
        var defParameters = this.taskDefinitionParameterMapper.findByTaskDefinitionId(keyVersion);
        var workerParameters = this.taskDefinitionWorkerParameterMapper.findByTaskDefinitionId(keyVersion);
        var parameterMap = workerParameters.stream()
                .map(workerParameterDO -> Map.entry(workerParameterDO.getParameterKey(), workerParameterDO.getParameterValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        definitionOptional.ifPresent(taskDefinition -> {
            taskDefinition.setParameters(defParameters);
            taskDefinition.setWorkerParameters(parameterMap);
        });
        return definitionOptional;
    }

    @Override
    public List<TaskDefinition> findAll() {
        return this.taskDefinitionMapper.findAll();
    }
}
