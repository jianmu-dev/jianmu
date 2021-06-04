package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.el.ElContext;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.infrastructure.mybatis.workflow.WorkflowInstanceRepositoryImpl;
import dev.jianmu.parameter.aggregate.Parameter;
import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.task.aggregate.InstanceParameter;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @program: workflow
 * @description: 流程实例门面类
 * @author: Ethan Liu
 * @create: 2021-01-22 14:50
 **/
@Service
public class WorkflowInstanceApplication {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceApplication.class);

    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepositoryImpl workflowInstanceRepository;
    private final WorkflowInstanceDomainService workflowInstanceDomainService;
    private final TaskInstanceRepository taskInstanceRepository;
    private final ExpressionLanguage expressionLanguage;
    private final InstanceParameterRepository instanceParameterRepository;
    private final ParameterRepository parameterRepository;

    @Inject
    public WorkflowInstanceApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepositoryImpl workflowInstanceRepository,
            WorkflowInstanceDomainService workflowInstanceDomainService,
            TaskInstanceRepository taskInstanceRepository,
            ExpressionLanguage expressionLanguage,
            InstanceParameterRepository instanceParameterRepository,
            ParameterRepository parameterRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowInstanceDomainService = workflowInstanceDomainService;
        this.taskInstanceRepository = taskInstanceRepository;
        this.expressionLanguage = expressionLanguage;
        this.instanceParameterRepository = instanceParameterRepository;
        this.parameterRepository = parameterRepository;
    }

    public Optional<WorkflowInstance> findById(String id) {
        return this.workflowInstanceRepository.findById(id);
    }

    public PageInfo<WorkflowInstance> findAllPage(String id, String name, String workflowVersion, ProcessStatus status, int pageNum, int pageSize) {
        return this.workflowInstanceRepository.findAllPage(id, name, workflowVersion, status, pageNum, pageSize);
    }

    public PageInfo<WorkflowInstance> findByWorkflowRef(String workflowRef) {
        return this.workflowInstanceRepository.findByWorkflowRef(workflowRef, 0, 20);
    }

    private EvaluationContext findContext(String instanceId) {
        var context = new ElContext();
        var instanceParameters = this.instanceParameterRepository.findByBusinessId(instanceId);
        var listMap = instanceParameters.stream()
                .collect(Collectors.groupingBy(InstanceParameter::getAsyncTaskRef));
        var ids = instanceParameters.stream()
                .map(InstanceParameter::getParameterId)
                .collect(Collectors.toSet());
        var parameters = this.parameterRepository.findByIds(ids);
        listMap.forEach((key, value) -> {
            var realValue = value.stream().map(instanceParameter -> {
                var k = instanceParameter.getRef();
                var val = parameters.stream()
                        .filter(parameter -> parameter.getId().equals(instanceParameter.getParameterId()))
                        .map(Parameter::getValue)
                        .findFirst().orElseThrow(() -> new DataNotFoundException("未找到匹配的参数"));
                return Map.entry(k, val);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            context.add(key, realValue);
        });
        return context;
    }

    // 创建并启动流程
    @Transactional
    public WorkflowInstance createAndStart(String triggerId, String workflowRefVersion) {
        Workflow workflow = this.workflowRepository
                .findByRefVersion(workflowRefVersion)
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 检查是否存在运行中的流程
        int i = this.workflowInstanceRepository
                .findByRefAndVersionAndStatus(workflow.getRef(), workflow.getVersion(), ProcessStatus.RUNNING)
                .size();
        if (i > 0) {
            throw new RuntimeException("该流程运行中");
        }
        // 创建新的流程实例
        WorkflowInstance workflowInstance = workflowInstanceDomainService.create(triggerId, workflow);
        workflowInstance.setExpressionLanguage(this.expressionLanguage);
        // 启动流程
        Node start = workflow.findStart();
        workflowInstanceDomainService.activateNode(workflow, workflowInstance, start.getRef());

        return this.workflowInstanceRepository.add(workflowInstance);
    }

    // 启动流程
    @Transactional
    public WorkflowInstance start(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义: " + instance.getWorkflowRef() + instance.getWorkflowVersion()));
        EvaluationContext context = this.findContext(instanceId);
        instance.setExpressionLanguage(this.expressionLanguage);
        instance.setContext(context);
        // 启动流程
        Node start = workflow.findNode(nodeRef);
        workflowInstanceDomainService.activateNode(workflow, instance, start.getRef());
        return this.workflowInstanceRepository.save(instance);
    }

    // 终止流程
    @Transactional
    public void stop(String instanceId) {
        var workflowInstance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var workflow = this.workflowRepository
                .findByRefAndVersion(workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 激活End节点
        workflowInstanceDomainService.finish(workflow, workflowInstance);
        this.workflowInstanceRepository.save(workflowInstance);
    }

    // 节点启动，重做
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public WorkflowInstance activateNode(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        EvaluationContext context = this.findContext(instanceId);
        instance.setExpressionLanguage(this.expressionLanguage);
        instance.setContext(context);
        // 激活节点
        logger.info("activateNode: " + nodeRef);
        workflowInstanceDomainService.activateNode(workflow, instance, nodeRef);
        return this.workflowInstanceRepository.save(instance);
    }

    @Recover
    public WorkflowInstance recoverActivateNode(DBException.OptimisticLocking e, String instanceId, String nodeRef) {
        logger.info("WorkflowInstance id {} 的节点 {} 无法激活", instanceId, nodeRef);
        logger.error("------------超过重试次数-------------", e);
        return null;
    }

    // 任务中止，完成
    @Transactional
    public WorkflowInstance terminateNode(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        instance.setExpressionLanguage(this.expressionLanguage);
        // 中止节点
        logger.info("terminateNode: " + nodeRef);
        workflowInstanceDomainService.terminateNode(workflow, instance, nodeRef);
        return this.workflowInstanceRepository.save(instance);
    }

    // 任务已启动命令
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void taskRun(String taskInstanceId) {
        var taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflowInstance = this.workflowInstanceRepository
                .findById(taskInstance.getBusinessId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.taskRun(taskInstance.getAsyncTaskRef());
        this.workflowInstanceRepository.save(workflowInstance);
    }

    @Recover
    public void recoverTask(DBException.OptimisticLocking e, String taskInstanceId) {
        logger.info("taskInstance id {} 的任务无法修改状态", taskInstanceId);
        logger.error("------------超过重试次数-------------", e);
    }

    // 任务已中止命令
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void taskFail(String taskInstanceId) {
        var taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflowInstance = this.workflowInstanceRepository
                .findById(taskInstance.getBusinessId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        workflowInstance.taskFail(taskInstance.getAsyncTaskRef());
        this.workflowInstanceRepository.save(workflowInstance);
    }

    // 任务已成功命令
    @Transactional
    @Retryable(value = DBException.OptimisticLocking.class, maxAttempts = 5, backoff = @Backoff(delay = 3000L, multiplier = 2))
    public void taskSucceed(String taskInstanceId) {
        var taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        var workflowInstance = this.workflowInstanceRepository
                .findById(taskInstance.getBusinessId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(workflowInstance.getWorkflowRef(), workflowInstance.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 任务执行成功
        logger.info("taskSucceed: " + taskInstance.getAsyncTaskRef());
        this.workflowInstanceDomainService.taskSucceed(workflow, workflowInstance, taskInstance.getAsyncTaskRef());
        this.workflowInstanceRepository.save(workflowInstance);
    }
    // TODO 任务跳过命令
}
