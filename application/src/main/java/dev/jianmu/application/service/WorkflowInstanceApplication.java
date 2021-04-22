package dev.jianmu.application.service;

import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mybatis.workflow.WorkflowInstanceRepositoryImpl;
import dev.jianmu.workflow.aggregate.definition.Node;
import dev.jianmu.workflow.aggregate.definition.Workflow;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.WorkflowRepository;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * @program: workflow
 * @description: 流程实例门面类
 * @author: Ethan Liu
 * @create: 2021-01-22 14:50
 **/
@Service
@Transactional
public class WorkflowInstanceApplication {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowInstanceApplication.class);

    private final WorkflowRepository workflowRepository;
    private final WorkflowInstanceRepositoryImpl workflowInstanceRepository;
    private final WorkflowInstanceDomainService workflowInstanceDomainService;
    private final ExpressionLanguage expressionLanguage;

    @Inject
    public WorkflowInstanceApplication(WorkflowRepository workflowRepository,
                                       WorkflowInstanceRepositoryImpl workflowInstanceRepository,
                                       WorkflowInstanceDomainService workflowInstanceDomainService,
                                       ExpressionLanguage expressionLanguage) {
        this.workflowRepository = workflowRepository;
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.workflowInstanceDomainService = workflowInstanceDomainService;
        this.expressionLanguage = expressionLanguage;
    }

    public PageInfo<WorkflowInstance> findAllPage(int pageNum, int pageSize) {
        return this.workflowInstanceRepository.findAllPage(pageNum, pageSize);
    }

    // 创建并启动流程
    public WorkflowInstance createAndStart(String triggerId, String workflowRefVersion) {
        Workflow workflow = this.workflowRepository
                .findByRefVersion(workflowRefVersion)
                .orElseThrow(() -> new RuntimeException("未找到流程定义"));
        // 检查是否存在运行中的流程
        int i = this.workflowInstanceRepository
                .findByRefAndVersionAndStatus(workflow.getRef(), workflow.getVersion(), ProcessStatus.RUNNING)
                .size();
        if (i > 0) {
            throw new RuntimeException("该流程运行中");
        }
        // TODO 需要查询流程定义对应的参数定义并创建参数实例，Parameter的Domain Service
        // 创建新的流程实例
        WorkflowInstance workflowInstance = workflowInstanceDomainService.create(triggerId, workflow);
        workflowInstance.setExpressionLanguage(this.expressionLanguage);
        // 启动流程
        Node start = workflow.findStart();
        workflowInstanceDomainService.activateNode(workflow, workflowInstance, start.getRef());

        return this.workflowInstanceRepository.add(workflowInstance);
    }

    // 启动流程
    public WorkflowInstance start(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new RuntimeException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new RuntimeException("未找到流程定义: " + instance.getWorkflowRef() + instance.getWorkflowVersion()));
        // TODO 需要查询流程定义对应的参数定义并创建参数实例，Parameter的Domain Service
        instance.setExpressionLanguage(this.expressionLanguage);
        // 启动流程
        Node start = workflow.findNode(nodeRef);
        workflowInstanceDomainService.activateNode(workflow, instance, start.getRef());
        return this.workflowInstanceRepository.save(instance);
    }

    // 节点启动，重做
    public WorkflowInstance activateNode(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new RuntimeException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new RuntimeException("未找到流程定义"));
        // TODO 需要查询定义对应的参数定义并创建参数实例，Parameter的Domain Service
        instance.setExpressionLanguage(this.expressionLanguage);
        // 激活节点
        logger.info("activateNode: " + nodeRef);
        workflowInstanceDomainService.activateNode(workflow, instance, nodeRef);
        return this.workflowInstanceRepository.save(instance);
    }

    // 任务中止，完成
    public WorkflowInstance terminateNode(String instanceId, String nodeRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new RuntimeException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new RuntimeException("未找到流程定义"));
        instance.setExpressionLanguage(this.expressionLanguage);
        // 中止节点
        logger.info("terminateNode: " + nodeRef);
        workflowInstanceDomainService.terminateNode(workflow, instance, nodeRef);
        return this.workflowInstanceRepository.save(instance);
    }

    // 任务已启动命令
    public void taskRun(String instanceId, String asyncTaskRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new RuntimeException("未找到该流程实例"));
        instance.taskRun(asyncTaskRef);
        this.workflowInstanceRepository.save(instance);
    }

    // 任务已中止命令
    public void taskFail(String instanceId, String asyncTaskRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new RuntimeException("未找到该流程实例"));
        instance.taskFail(asyncTaskRef);
        this.workflowInstanceRepository.save(instance);
    }

    // 任务已成功命令
    public void taskSucceed(String instanceId, String asyncTaskRef) {
        WorkflowInstance instance = this.workflowInstanceRepository
                .findById(instanceId)
                .orElseThrow(() -> new RuntimeException("未找到该流程实例"));
        Workflow workflow = this.workflowRepository
                .findByRefAndVersion(instance.getWorkflowRef(), instance.getWorkflowVersion())
                .orElseThrow(() -> new RuntimeException("未找到流程定义"));
        // 任务执行成功
        logger.info("taskSucceed: " + asyncTaskRef);
        this.workflowInstanceDomainService.taskSucceed(workflow, instance, asyncTaskRef);
        this.workflowInstanceRepository.save(instance);
    }
    // TODO 任务跳过命令
}
