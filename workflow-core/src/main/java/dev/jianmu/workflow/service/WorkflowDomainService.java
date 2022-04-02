package dev.jianmu.workflow.service;

import dev.jianmu.workflow.aggregate.definition.LoopPair;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import dev.jianmu.workflow.aggregate.process.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    public boolean canSkipNode(String nodeRef, String sender, Workflow workflow, List<AsyncTaskInstance> asyncTaskInstances) {
        // 返回当前节点上游Task的ref List
        var node = workflow.findNode(nodeRef);
        // 获取环路对上游节点列表
        var loopSources = node.getLoopPairs().stream()
                .map(LoopPair::getSource)
                .filter(source -> !source.equals(sender))
                .collect(Collectors.toList());
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
        // 计算上游节点完成次数是否相同
        List<AsyncTaskInstance> allSources = new ArrayList<>(sources);
        allSources.addAll(gatewaySources);
        var sets = allSources.stream()
                .map(AsyncTaskInstance::getSerialNo)
                .collect(Collectors.toSet())
                .size();
        // 如果上游没有环路并且大于1意味着存在不同次数的节点，不能跳过
        if (sets > 1) {
            logger.info("找到不同次数的节点，不能跳过");
            return false;
        }
        // 根据上游节点列表，统计已跳过的任务数量
        long taskSkipped = sources.stream()
                .filter(t -> t.getStatus().equals(TaskStatus.SKIPPED))
                .count();
        long gatewaySkipped = gatewaySources.stream()
                .filter(t -> !t.getStatus().equals(TaskStatus.INIT))
                .filter(t -> !t.isNextTarget(nodeRef))
                .count();
        var gatewayActivated = gatewaySources.stream()
                .filter(t -> !t.getStatus().equals(TaskStatus.INIT))
                .filter(t -> t.isNextTarget(nodeRef))
                .map(AsyncTaskInstance::getAsyncTaskRef)
                .collect(Collectors.toList());
        logger.info("当前节点{}上游Task数量为{}", nodeRef, refList.size());
        logger.info("当前节点{}上游Task已跳过数量为{}", nodeRef, taskSkipped);
        logger.info("当前节点{}上游Gateway数量为{}", nodeRef, gatewaySources.size());
        logger.info("当前节点{}上游Gateway已跳过数量为{}", nodeRef, gatewaySkipped);
        logger.info("当前节点{}上游激活Gateway的数量为{}", nodeRef, gatewayActivated.size());
        loopSources.removeAll(gatewayActivated);
        logger.info("当前节点{}上游环路对数量为{}", nodeRef, loopSources.size());
        var skipped = taskSkipped + gatewaySkipped;
        // 如果上游任务执行完成数量小于上游任务总数，则当前节点不激活
        if (skipped < (refList.size() + gatewaySources.size())) {
            logger.info("当前节点{}上游节点已跳过数量{}小于上游节点总数{}，不能跳过", nodeRef, skipped, refList.size() + gatewaySources.size());
            return false;
        }
        return true;
    }
}
