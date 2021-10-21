package dev.jianmu.workflow.service;

import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: workflow
 * @description: 流程实例DomainService
 * @author: Ethan Liu
 * @create: 2021-01-21 21:16
 **/
public class WorkflowInstanceDomainService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceDomainService.class);

    public WorkflowInstance create(String triggerId, String triggerType, int serialNo, Workflow workflow) {
        // 构造流程实例
        return WorkflowInstance.Builder.aWorkflowInstance()
                .serialNo(serialNo)
                .triggerId(triggerId)
                .triggerType(triggerType)
                .name(workflow.getName())
                .description(workflow.getDescription())
                .workflowRef(workflow.getRef())
                .workflowVersion(workflow.getVersion())
                .build();
    }

    // 激活节点
    public void activateNode(Workflow workflow, WorkflowInstance workflowInstance, String nodeRef) {
        Node node = workflow.findNode(nodeRef);
        // 返回当前节点上游Task的ref List
        List<String> refList = workflow.findTasks(nodeRef);
        List<String> instanceList = workflowInstance.getAsyncTaskInstances().stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        // 统计上游Task已完成数量
        long completed = workflowInstance.countCompletedTask(instanceList);
        logger.info("当前节点{}上游Task数量为{}", nodeRef, refList.size());
        logger.info("当前节点{}上游Task已完成数量为{}", nodeRef, completed);
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (completed < refList.size()) {
            logger.info("当前节点{}上游任务执行完成数量{}小于上游任务总数{}", nodeRef, completed, refList.size());
            return;
        }
        workflowInstance.activateNode(node);
    }

    // 跳过节点
    public void skipNode(Workflow workflow, WorkflowInstance workflowInstance, String nodeRef) {
        Node node = workflow.findNode(nodeRef);
        workflowInstance.skipNode(node);
    }

    // 中止节点
    public void terminateNode(Workflow workflow, WorkflowInstance workflowInstance, String nodeRef) {
        if (workflowInstance.getStatus().equals(ProcessStatus.FINISHED)) {
            throw new RuntimeException("该流程实例已结束，不能中止节点");
        }
        if (workflowInstance.getStatus().equals(ProcessStatus.TERMINATED)) {
            throw new RuntimeException("该流程实例已终止，不能中止节点");
        }
        Node node = workflow.getNodes().stream()
                .filter(n -> n.getRef().equals(nodeRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到要中止的节点: " + nodeRef));
        workflowInstance.terminateNode(node);
    }

    // 任务执行成功
    public void taskSucceed(Workflow workflow, WorkflowInstance workflowInstance, String asyncTaskRef) {
        Node node = workflow.getNodes().stream()
                .filter(n -> n.getRef().equals(asyncTaskRef))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到执行完成的任务节点: " + asyncTaskRef));
        workflowInstance.taskSucceed(node);
    }
}
