package dev.jianmu.application.service.internal;

import dev.jianmu.application.command.ActivateNodeCmd;
import dev.jianmu.application.command.NextNodeCmd;
import dev.jianmu.application.command.SkipNodeCmd;
import dev.jianmu.application.command.WorkflowStartCmd;
import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.el.ElContext;
import dev.jianmu.task.repository.InstanceParameterRepository;
import dev.jianmu.trigger.event.TriggerEvent;
import dev.jianmu.trigger.repository.TriggerEventRepository;
import dev.jianmu.workflow.aggregate.definition.AsyncTask;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.parameter.Parameter;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.AsyncTaskInstanceRepository;
import dev.jianmu.workflow.repository.ParameterRepository;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.ParameterDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class WorkflowInternalApplication
 * @description WorkflowInternalApplication
 * @create 2022-01-01 10:53
 */
@Service
@Slf4j
public class WorkflowInternalApplication {
    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AsyncTaskInstanceRepository asyncTaskInstanceRepository;
    private final ExpressionLanguage expressionLanguage;
    private final InstanceParameterRepository instanceParameterRepository;
    private final ParameterDomainService parameterDomainService;
    private final TriggerEventRepository triggerEventRepository;
    private final ParameterRepository parameterRepository;

    public WorkflowInternalApplication(
            WorkflowRepository workflowRepository,
            WorkflowInstanceRepository workflowInstanceRepository,
            AsyncTaskInstanceRepository asyncTaskInstanceRepository,
            ExpressionLanguage expressionLanguage,
            InstanceParameterRepository instanceParameterRepository,
            ParameterDomainService parameterDomainService,
            TriggerEventRepository triggerEventRepository,
            ParameterRepository parameterRepository
    ) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.asyncTaskInstanceRepository = asyncTaskInstanceRepository;
        this.expressionLanguage = expressionLanguage;
        this.instanceParameterRepository = instanceParameterRepository;
        this.parameterDomainService = parameterDomainService;
        this.triggerEventRepository = triggerEventRepository;
        this.parameterRepository = parameterRepository;
    }

    private EvaluationContext findContext(Workflow workflow, String triggerId) {
        // 查询参数源
        var eventParameters = this.triggerEventRepository.findById(triggerId)
                .map(TriggerEvent::getParameters)
                .orElseGet(List::of);
        var instanceParameters = this.instanceParameterRepository
                .findOutputParamByTriggerId(triggerId);
        // 创建表达式上下文
        var context = new ElContext();
        // 全局参数加入上下文
        workflow.getGlobalParameters()
                .forEach(globalParameter -> context.add(
                        "global",
                        globalParameter.getName(),
                        Parameter.Type.getTypeByName(globalParameter.getType()).newParameter(globalParameter.getValue()))
                );
        // 事件参数加入上下文
        var eventParams = eventParameters.stream()
                .map(eventParameter -> Map.entry(eventParameter.getName(), eventParameter.getParameterId()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var eventParamValues = this.parameterRepository.findByIds(new HashSet<>(eventParams.values()));
        var eventMap = this.parameterDomainService.matchParameters(eventParams, eventParamValues);
        // 事件参数scope为event
        eventMap.forEach((key, val) -> context.add("trigger", key, val));
        // 任务输出参数加入上下文
        Map<String, String> outParams = new HashMap<>();
        instanceParameters.forEach(instanceParameter -> {
            // 输出参数scope为workflowType.asyncTaskRef
            outParams.put(instanceParameter.getWorkflowType() + "." + instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId());
            // 输出参数scope为asyncTaskRef
            outParams.put(instanceParameter.getAsyncTaskRef() + "." + instanceParameter.getRef(), instanceParameter.getParameterId());
        });
        var outParamValues = this.parameterRepository.findByIds(new HashSet<>(outParams.values()));
        var outMap = this.parameterDomainService.matchParameters(outParams, outParamValues);
        outMap.forEach(context::add);
        return context;
    }

    public void start(WorkflowStartCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        // 启动流程
        workflow.start(cmd.getTriggerId());
        this.workflowRepository.commitEvents(workflow);
    }

    // 根据上游节点列表，统计已完成的任务数量
    public long countCompletedTask(List<AsyncTaskInstance> asyncTaskInstances, List<String> refList) {
        return asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()) &&
                        (
                                t.getStatus().equals(TaskStatus.FAILED)
                                        || t.getStatus().equals(TaskStatus.SUCCEEDED)
                                        || t.getStatus().equals(TaskStatus.SKIPPED)
                        ))
                .count();
    }

    public void next(NextNodeCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        workflow.next(cmd.getTriggerId(), cmd.getNodeRef());
        this.workflowRepository.commitEvents(workflow);
    }

    // 节点启动
    public void activateNode(ActivateNodeCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        EvaluationContext context = this.findContext(workflow, cmd.getTriggerId());
        workflow.setExpressionLanguage(this.expressionLanguage);
        workflow.setContext(context);
        // 激活节点
        var asyncTaskInstances = this.asyncTaskInstanceRepository.findByTriggerId(cmd.getTriggerId());
        // 返回当前节点上游Task的ref List
        List<String> refList = workflow.findTasks(cmd.getNodeRef());
        List<String> instanceList = asyncTaskInstances.stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        // 统计上游Task已完成数量
        long completed = this.countCompletedTask(asyncTaskInstances, instanceList);
        log.info("当前节点{}上游Task数量为{}", cmd.getNodeRef(), refList.size());
        log.info("当前节点{}上游Task已完成数量为{}", cmd.getNodeRef(), completed);
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (completed < refList.size()) {
            log.info("当前节点{}上游任务执行完成数量{}小于上游任务总数{}", cmd.getNodeRef(), completed, refList.size());
            return;
        }
        log.info("activateNode: " + cmd.getNodeRef());
        workflow.activateNode(cmd.getTriggerId(), cmd.getNodeRef());
        this.workflowRepository.commitEvents(workflow);
    }

    // 节点跳过
    public void skipNode(SkipNodeCmd cmd) {
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(cmd.getWorkflowRef(), cmd.getWorkflowVersion())
                .orElseThrow(() -> new DataNotFoundException("未找到流程定义"));
        var workflowInstance = this.workflowInstanceRepository.findByTriggerId(cmd.getTriggerId())
                .orElseThrow(() -> new DataNotFoundException("未找到该流程实例"));
        var node = workflow.findNode(cmd.getNodeRef());
        var list = this.asyncTaskInstanceRepository
                .findByTriggerIdAndTaskRef(cmd.getTriggerId(), cmd.getNodeRef());
        if (list.size() > 0) {
            log.info("发现逻辑回环，忽略跳过事件");
            return;
        }

        if (node instanceof AsyncTask) {
            var asyncTaskInstance = AsyncTaskInstance.Builder
                    .anAsyncTaskInstance()
                    .workflowInstanceId(workflowInstance.getId())
                    .triggerId(cmd.getTriggerId())
                    .workflowRef(cmd.getWorkflowRef())
                    .workflowVersion(cmd.getWorkflowVersion())
                    .name(node.getName())
                    .description(node.getDescription())
                    .asyncTaskRef(node.getRef())
                    .asyncTaskType(node.getType())
                    .build();
            asyncTaskInstance.skip();
            log.info("创建跳过状态的异步任务");
            this.asyncTaskInstanceRepository.add(asyncTaskInstance);
        }

        workflow.skipNode(cmd.getTriggerId(), cmd.getNodeRef());
        this.workflowRepository.commitEvents(workflow);
    }
}
