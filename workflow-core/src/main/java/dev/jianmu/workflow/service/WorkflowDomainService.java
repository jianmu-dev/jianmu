package dev.jianmu.workflow.service;

import dev.jianmu.workflow.aggregate.definition.LoopPair;
import dev.jianmu.workflow.aggregate.definition.Start;
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

    public boolean canActivateNode(String nodeRef, String sender, Workflow workflow, List<AsyncTaskInstance> asyncTaskInstances) {
        // 返回当前节点上游Task的ref List
        var node = workflow.findNode(nodeRef);
        var senderNode = workflow.findNode(sender);
        if (!(senderNode instanceof Start)) {
            // 串行并发汇聚检查
            var loopSource = node.getLoopPairs().stream()
                    .map(LoopPair::getSource)
                    .filter(source -> source.equals(sender))
                    .count();
            var sourceTask = asyncTaskInstances.stream()
                    .filter(t -> t.getAsyncTaskRef().equals(sender))
                    .findFirst().orElseThrow(() -> new RuntimeException("未找到事件发送节点任务"));
            var nodeTask = asyncTaskInstances.stream()
                    .filter(t -> t.getAsyncTaskRef().equals(nodeRef))
                    .findFirst().orElseThrow(() -> new RuntimeException("未找到待激活节点任务"));
            if (loopSource == 0) {
                // 如果事件发送者不在环路中
                if (nodeTask.getVersion() >= sourceTask.getVersion()) {
                    logger.warn("非环路: 当前节点已执行，不触发激活事件");
                    return false;
                }
            } else {
                // 如果事件发送者在环路中
                if (nodeTask.getVersion() > sourceTask.getVersion()) {
                    logger.warn("环路: 当前节点已执行，不触发激活事件");
                    return false;
                }
            }
        }
        // 获取环路下游任务列表，不包含触发环路
        var loopTargets = node.getLoopPairs().stream()
                .filter(loopPair -> !loopPair.getSource().equals(sender))
                .map(LoopPair::getTarget)
                .collect(Collectors.toList());
        // 根据LoopPairs统计环路下游非运行状态任务数量
        long loop = asyncTaskInstances.stream()
                .filter(t -> loopTargets.contains(t.getAsyncTaskRef()))
                .filter(t -> !t.getStatus().equals(TaskStatus.RUNNING))
                .count();
        List<String> refList = workflow.findNodes(nodeRef);
        List<String> instanceList = asyncTaskInstances.stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        // 根据上游节点列表，统计已完成的任务数量
        var completedSources = asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()) &&
                        (
                                t.getStatus().equals(TaskStatus.FAILED)
                                        || t.getStatus().equals(TaskStatus.SUCCEEDED)
                                        || t.getStatus().equals(TaskStatus.SKIPPED)
                        ))
                .collect(Collectors.toList());
        logger.info("当前节点{}上游Task数量为{}", nodeRef, refList.size());
        logger.info("当前节点{}上游Task已完成数量为{}", nodeRef, completedSources.size());
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (completedSources.size() < refList.size()) {
            logger.info("当前节点{}上游任务执行完成数量{}小于上游任务总数{}", nodeRef, completedSources.size(), refList.size());
            if (loopTargets.size() > 0 && loop == loopTargets.size()) {
                logger.info("环路检测: 环路对下游数量为{}, 未执行状态的任务数量为{}, 可以继续触发", loopTargets.size(), loop);
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean hasSameSerialNo(String nodeRef, Workflow workflow, List<AsyncTaskInstance> asyncTaskInstances) {
        List<String> refList = workflow.findNodes(nodeRef);
        List<String> instanceList = asyncTaskInstances.stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        // 上游节点实例列表
        var sources = asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()))
                .collect(Collectors.toList());
        var sets = sources.stream()
                .map(AsyncTaskInstance::getSerialNo)
                .collect(Collectors.toSet())
                .size();
        // 如果大于1意味着存在不同次数的节点，不能跳过
        return sets <= 1;
    }

    public boolean canSkipNode(String nodeRef, String sender, Workflow workflow, List<AsyncTaskInstance> asyncTaskInstances) {
        // 返回当前节点上游Task的ref List
        var node = workflow.findNode(nodeRef);
        // 获取环路下游任务列表，不包含触发环路
        var loopTargets = node.getLoopPairs().stream()
                .filter(loopPair -> !loopPair.getSource().equals(sender))
                .map(LoopPair::getTarget)
                .collect(Collectors.toList());
        // 根据LoopPairs统计环路下游非运行状态任务数量
        long loop = asyncTaskInstances.stream()
                .filter(t -> loopTargets.contains(t.getAsyncTaskRef()))
                .filter(t -> t.getStatus().equals(TaskStatus.INIT))
                .count();
        if (!this.hasSameSerialNo(nodeRef, workflow, asyncTaskInstances)) {
            logger.info("上游节点执行次数不同，检测下游环路");
            if (loopTargets.size() == 0) {
                logger.info("不存在环路，不能跳过");
                return false;
            }
            if (loop == loopTargets.size()) {
                logger.info("环路检测: 环路对下游数量为{}, 未执行状态的任务数量为{}, 可以跳过", loopTargets.size(), loop);
                return true;
            }
        }
        List<String> refList = workflow.findNodesWithoutGateway(nodeRef);
        List<String> gatewayRefs = workflow.findGateWay(nodeRef);
        List<String> instanceList = asyncTaskInstances.stream()
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        instanceList.retainAll(refList);
        instanceList.retainAll(gatewayRefs);
        // 上游节点实例列表
        var sources = asyncTaskInstances.stream()
                .filter(t -> refList.contains(t.getAsyncTaskRef()))
                .collect(Collectors.toList());
        var gatewaySources = asyncTaskInstances.stream()
                .filter(t -> gatewayRefs.contains(t.getAsyncTaskRef()))
                .collect(Collectors.toList());
        // 根据上游节点列表，统计已跳过的任务数量
        long taskSkipped = sources.stream()
                .filter(t -> t.getStatus().equals(TaskStatus.SKIPPED))
                .count();
        long gatewaySkipped = gatewaySources.stream()
                .filter(t -> !t.getStatus().equals(TaskStatus.INIT))
                .filter(t -> !t.isNextTarget(nodeRef))
                .count();
        logger.info("当前节点{}上游Task数量为{}", nodeRef, refList.size());
        logger.info("当前节点{}上游Task已跳过数量为{}", nodeRef, taskSkipped);
        logger.info("当前节点{}上游Gateway数量为{}", nodeRef, gatewaySources.size());
        logger.info("当前节点{}上游Gateway已跳过数量为{}", nodeRef, gatewaySkipped);
        var skipped = taskSkipped + gatewaySkipped;
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (skipped < (refList.size() + gatewaySources.size())) {
            logger.info("当前节点{}上游节点已跳过数量{}小于上游节点总数{}，不能跳过", nodeRef, skipped, refList.size() + gatewaySources.size());
            return false;
        }
        return true;
    }
}
