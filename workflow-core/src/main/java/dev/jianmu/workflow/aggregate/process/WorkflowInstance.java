package dev.jianmu.workflow.aggregate.process;

import dev.jianmu.workflow.aggregate.AggregateRoot;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.el.EvaluationContext;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.event.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: workflow
 * @description: 流程运行实例
 * @author: Ethan Liu
 * @create: 2021-01-21 19:53
 **/
public class WorkflowInstance extends AggregateRoot {
    // ID
    private String id;
    // 执行顺序号
    private int serialNo;
    // 触发器ID
    private String triggerId;
    // 触发器类型
    private String triggerType;
    // 显示名称
    private String name;
    // 描述
    private String description;
    // 运行模式
    private RunMode runMode = RunMode.AUTO;
    // 运行状态
    private ProcessStatus status = ProcessStatus.RUNNING;
    // 流程定义唯一引用名称
    private String workflowRef;
    // 流程定义版本
    private String workflowVersion;
    // 任务实例列表
    private List<AsyncTaskInstance> asyncTaskInstances = new ArrayList<>();
    // 开始时间
    private final LocalDateTime startTime = LocalDateTime.now();
    // 结束时间
    private LocalDateTime endTime;
    // 表达式计算服务
    private ExpressionLanguage expressionLanguage;
    // 参数上下文
    private EvaluationContext context;

    private WorkflowInstance() {
    }

    public void setExpressionLanguage(ExpressionLanguage expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public void setContext(EvaluationContext context) {
        this.context = context;
    }

    public Optional<AsyncTaskInstance> findLatestAsyncTaskInstance() {
        if (asyncTaskInstances.isEmpty()) {
            return Optional.empty();
        }
        Comparator<AsyncTaskInstance> byStartTime = Comparator.comparing(AsyncTaskInstance::getStartTime);
        return asyncTaskInstances.stream().filter(asyncTaskInstance -> asyncTaskInstance.getStartTime() != null).max(byStartTime);
    }

    // 根据上游节点列表，统计已完成的任务数量
    public long countCompletedTask(List<String> refList) {
        return this.asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()) &&
                        (
                                t.getStatus().equals(TaskStatus.FAILED)
                                        || t.getStatus().equals(TaskStatus.SUCCEEDED)
                                        || t.getStatus().equals(TaskStatus.SKIPPED)
                        ))
                .count();
    }

    // 终止流程实例
    public void terminate() {
        this.status = ProcessStatus.TERMINATED;
        this.endTime = LocalDateTime.now();
    }

    // 激活节点
    public void activateNode(Node node) {
        if (this.getStatus().equals(ProcessStatus.FINISHED)) {
            throw new RuntimeException("该流程实例已结束");
        }
        if (this.getStatus().equals(ProcessStatus.TERMINATED)) {
            throw new RuntimeException("该流程实例已终止");
        }
        if (node instanceof End) {
            // 发布流程结束事件并返回
            WorkflowEndEvent workflowEndEvent = WorkflowEndEvent.Builder.aWorkflowEndEvent()
                    .nodeRef(node.getRef())
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .build();
            this.raiseEvent(workflowEndEvent);
            this.status = ProcessStatus.FINISHED;
            this.endTime = LocalDateTime.now();
            return;
        }
        if (node instanceof AsyncTask) {
            // 触发此任务节点执行的时候才创建任务实例
            AsyncTaskInstance taskInstance = this.findInstanceByRef(node.getRef())
                    .orElse(
                            AsyncTaskInstance.Builder
                                    .anAsyncTaskInstance()
                                    .name(node.getName())
                                    .description(node.getDescription())
                                    .asyncTaskRef(node.getRef())
                                    .asyncTaskType(node.getType())
                                    .build()
                    );
            // 处理反序列化后asyncTaskInstances为不可变List的情况
            List<AsyncTaskInstance> instances = new ArrayList<>(this.asyncTaskInstances);
            instances.removeIf(i -> i.getAsyncTaskRef().equals(node.getRef()));
            instances.add(taskInstance);
            this.asyncTaskInstances = instances;
            // 发布任务激活事件并返回
            TaskActivatingEvent taskActivatingEvent = TaskActivatingEvent.Builder.aTaskActivatingEvent()
                    .nodeRef(node.getRef())
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .nodeType(node.getType())
                    .build();
            this.raiseEvent(taskActivatingEvent);
            return;
        }
        if (node instanceof Gateway) {
            String nextNodeRef = ((Gateway) node).calculateTarget(expressionLanguage, context);
            // 发布其他节点跳过事件
            var targets = node.getTargets().stream()
                    .filter(targetRef -> !targetRef.equals(nextNodeRef))
                    .collect(Collectors.toList());
            targets.forEach(targetRef -> {
                var nodeSkipEvent = NodeSkipEvent.Builder.aNodeSkipEvent()
                        .nodeRef(targetRef)
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.id)
                        .workflowRef(this.workflowRef)
                        .workflowVersion(this.workflowVersion)
                        .build();
                this.raiseEvent(nodeSkipEvent);
            });
            // 发布下一个节点激活事件并返回
            NodeActivatingEvent activatingEvent = NodeActivatingEvent.Builder.aNodeActivatingEvent()
                    .nodeRef(nextNodeRef)
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .build();
            this.raiseEvent(activatingEvent);
            return;
        }
        if (node instanceof Start) {
            // 发布流程启动事件
            WorkflowStartEvent workflowStartEvent = WorkflowStartEvent.Builder.aWorkflowStartEvent()
                    .nodeRef(node.getRef())
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .build();
            this.raiseEvent(workflowStartEvent);
        }
        // 发布所有下游节点激活事件
        Set<String> nodes = node.getTargets();
        nodes.forEach(n -> {
            NodeActivatingEvent activatingEvent = NodeActivatingEvent.Builder.aNodeActivatingEvent()
                    .nodeRef(n)
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .build();
            this.raiseEvent(activatingEvent);
        });
    }

    // 跳过节点
    public void skipNode(Node node) {
        if (node instanceof End) {
            return;
        }
        if (node instanceof AsyncTask) {
            // 跳过任务节点时也需要创建任务实例，状态为已跳过
            AsyncTaskInstance taskInstance = this.findInstanceByRef(node.getRef())
                    .orElse(
                            AsyncTaskInstance.Builder
                                    .anAsyncTaskInstance()
                                    .name(node.getName())
                                    .description(node.getDescription())
                                    .asyncTaskRef(node.getRef())
                                    .asyncTaskType(node.getType())
                                    .build()
                    );
            taskInstance.skip();
            // 处理反序列化后asyncTaskInstances为不可变List的情况
            List<AsyncTaskInstance> instances = new ArrayList<>(this.asyncTaskInstances);
            instances.removeIf(i -> i.getAsyncTaskRef().equals(node.getRef()));
            instances.add(taskInstance);
            this.asyncTaskInstances = instances;
        }
        // 发布下游节点跳过事件
        var targets = node.getTargets();
        targets.forEach(targetRef -> {
            var nodeSkipEvent = NodeSkipEvent.Builder.aNodeSkipEvent()
                    .nodeRef(targetRef)
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .build();
            this.raiseEvent(nodeSkipEvent);
        });
    }

    // 中止节点, 非任务类节点无法中止
    public void terminateNode(Node node) {
        if (node instanceof AsyncTask) {
            // 发布任务中止事件
            AsyncTaskInstance taskInstance = this.findInstanceByRef(node.getRef())
                    .orElseThrow(() -> new RuntimeException("未找到任务"));
            TaskTerminatingEvent terminatingEvent = TaskTerminatingEvent.Builder.aTaskTerminatingEvent()
                    .nodeRef(taskInstance.getAsyncTaskRef())
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .nodeType(taskInstance.getAsyncTaskType())
                    .externalId(taskInstance.getExternalId())
                    .build();
            this.raiseEvent(terminatingEvent);
        }
    }

    // 异步任务开始执行
    public void taskRun(String asyncTaskRef, String externalId) {
        AsyncTaskInstance taskInstance = this.findInstanceByRef(asyncTaskRef)
                .orElseThrow(() -> new RuntimeException("未找到该任务"));
        taskInstance.run(externalId);
        // 发布任务开始执行事件
        this.raiseEvent(
                TaskRunningEvent.Builder.aTaskRunningEvent()
                        .nodeRef(taskInstance.getAsyncTaskRef())
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.getId())
                        .workflowRef(this.getWorkflowRef())
                        .workflowVersion(this.getWorkflowVersion())
                        .nodeType(taskInstance.getAsyncTaskType())
                        .externalId(taskInstance.getExternalId())
                        .build()
        );
    }

    // 异步任务执行失败
    public void taskFail(String asyncTaskRef) {
        AsyncTaskInstance taskInstance = this.findInstanceByRef(asyncTaskRef)
                .orElseThrow(() -> new RuntimeException("未找到该任务"));
        taskInstance.fail();
        // 发布任务执行失败事件
        this.raiseEvent(
                TaskFailedEvent.Builder.aTaskFailedEvent()
                        .nodeRef(taskInstance.getAsyncTaskRef())
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.getId())
                        .workflowRef(this.getWorkflowRef())
                        .workflowVersion(this.getWorkflowVersion())
                        .nodeType(taskInstance.getAsyncTaskType())
                        .externalId(taskInstance.getExternalId())
                        .build()
        );
    }

    // 异步任务执行成功
    public void taskSucceed(Node node) {
        AsyncTaskInstance taskInstance = this.findInstanceByRef(node.getRef())
                .orElseThrow(() -> new RuntimeException("未找到该任务"));
        taskInstance.succeed();
        // 发布任务执行成功事件
        this.raiseEvent(
                TaskSucceededEvent.Builder.aTaskSucceededEvent()
                        .nodeRef(taskInstance.getAsyncTaskRef())
                        .triggerId(this.triggerId)
                        .workflowInstanceId(this.getId())
                        .workflowRef(this.getWorkflowRef())
                        .workflowVersion(this.getWorkflowVersion())
                        .nodeType(taskInstance.getAsyncTaskType())
                        .externalId(taskInstance.getExternalId())
                        .build()
        );
        // 发布所有下游节点激活事件
        Set<String> nodes = node.getTargets();
        nodes.forEach(n -> {
            NodeActivatingEvent activatingEvent = NodeActivatingEvent.Builder.aNodeActivatingEvent()
                    .nodeRef(n)
                    .triggerId(this.triggerId)
                    .workflowInstanceId(this.id)
                    .workflowRef(this.workflowRef)
                    .workflowVersion(this.workflowVersion)
                    .build();
            this.raiseEvent(activatingEvent);
        });
    }

    // 根据任务定义Ref查找任务实例
    public Optional<AsyncTaskInstance> findInstanceByRef(String asyncTaskRef) {
        return asyncTaskInstances.stream()
                .filter(i -> i.getAsyncTaskRef().equals(asyncTaskRef))
                .findFirst();
    }

    public String getId() {
        return id;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public String getWorkflowRef() {
        return workflowRef;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public List<AsyncTaskInstance> getAsyncTaskInstances() {
        return List.copyOf(asyncTaskInstances);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public static final class Builder {
        // ID
        // TODO 暂时使用UUID的值
        private String id = UUID.randomUUID().toString().replace("-", "");
        // 执行顺序号
        private int serialNo;
        // 触发器ID
        private String triggerId;
        // 触发器类型
        private String triggerType;
        // 显示名称
        private String name;
        // 描述
        private String description;
        // 流程定义唯一引用名称
        private String workflowRef;
        // 流程定义版本
        private String workflowVersion;

        private Builder() {
        }

        public static Builder aWorkflowInstance() {
            return new Builder();
        }

        public Builder serialNo(int serialNo) {
            this.serialNo = serialNo;
            return this;
        }

        public Builder triggerId(String triggerId) {
            this.triggerId = triggerId;
            return this;
        }

        public Builder triggerType(String triggerType) {
            this.triggerType = triggerType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder workflowRef(String workflowRef) {
            this.workflowRef = workflowRef;
            return this;
        }

        public Builder workflowVersion(String workflowVersion) {
            this.workflowVersion = workflowVersion;
            return this;
        }

        public WorkflowInstance build() {
            WorkflowInstance workflowInstance = new WorkflowInstance();
            workflowInstance.workflowVersion = this.workflowVersion;
            workflowInstance.serialNo = this.serialNo;
            workflowInstance.triggerId = this.triggerId;
            workflowInstance.triggerType = this.triggerType;
            workflowInstance.name = this.name;
            workflowInstance.description = this.description;
            workflowInstance.id = this.id;
            workflowInstance.workflowRef = this.workflowRef;
            return workflowInstance;
        }
    }
}
