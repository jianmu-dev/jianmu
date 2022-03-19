package dev.jianmu.workflow.service;

import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ethan Liu
 * @class WorkflowDomainService
 * @description WorkflowDomainService
 * @create 2022-03-08 21:31
 */
public class WorkflowDomainService {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowDomainService.class);

    public boolean canActivateNode(String nodeRef, Workflow workflow, List<AsyncTaskInstance> asyncTaskInstances) {
        // 返回当前节点上游Task的ref List
        List<String> refList = workflow.findNodes(nodeRef);
        List<String> instanceList = asyncTaskInstances.stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        // 根据上游节点列表，统计已完成的任务数量
        long completed = asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()) &&
                        (
                                t.getStatus().equals(TaskStatus.FAILED)
                                        || t.getStatus().equals(TaskStatus.SUCCEEDED)
                                        || t.getStatus().equals(TaskStatus.SKIPPED)
                        ))
                .count();
        logger.info("当前节点{}上游Task数量为{}", nodeRef, refList.size());
        logger.info("当前节点{}上游Task已完成数量为{}", nodeRef, completed);
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (completed < refList.size()) {
            logger.info("当前节点{}上游任务执行完成数量{}小于上游任务总数{}", nodeRef, completed, refList.size());
            return false;
        }
        return true;
    }

    public boolean canSkipNode(String nodeRef, Workflow workflow, List<AsyncTaskInstance> asyncTaskInstances) {
        // 返回当前节点上游Task的ref List
        List<String> refList = workflow.findNodes(nodeRef);
        List<String> instanceList = asyncTaskInstances.stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        // 根据上游节点列表，统计已跳过的任务数量
        long skipped = asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()) && (t.getStatus().equals(TaskStatus.SKIPPED)))
                .count();
        logger.info("当前节点{}上游Task数量为{}", nodeRef, refList.size());
        logger.info("当前节点{}上游Task已跳过数量为{}", nodeRef, skipped);
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (skipped == refList.size()) {
            logger.info("当前节点{}上游任务已跳过数量{}等于上游任务总数{}，继续跳过", nodeRef, skipped, refList.size());
            return true;
        }
        return false;
    }
}
