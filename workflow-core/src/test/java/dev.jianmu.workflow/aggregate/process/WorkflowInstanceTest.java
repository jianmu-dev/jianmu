package dev.jianmu.workflow.aggregate.process;

import dev.jianmu.workflow.PrintEventSubscriber;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.event.*;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @program: workflow
 * @description: WorkflowInstance测试类
 * @author: Ethan Liu
 * @create: 2021-01-22 16:53
 **/
@DisplayName("流程实例测试类")
public class WorkflowInstanceTest {
    private static Workflow workflow;
    private static WorkflowInstanceDomainService workflowInstanceDomainService = new WorkflowInstanceDomainService();
    private static WorkflowInstance instance;

    @BeforeAll
    static void beforeAll() {
        PrintEventSubscriber.sub();
        Start start = Start.Builder.aStart()
                .name("Start1")
                .ref("start_1")
                .description("开始节点1")
                .build();
        AsyncTask asyncTask1 = AsyncTask.Builder.anAsyncTask()
                .name("AsyncTask1")
                .ref("asyncTask_1")
                .description("异步任务节点1")
                .build();
        End end = End.Builder.anEnd()
                .name("End1")
                .ref("end_1")
                .description("结束节点1")
                .build();
        start.setTargets(Set.of(asyncTask1.getRef()));
        asyncTask1.setSources(Set.of(start.getRef()));
        asyncTask1.setTargets(Set.of(end.getRef()));
        end.setSources(Set.of(asyncTask1.getRef()));

        Set<Node> nodes = Set.of(start, asyncTask1, end);
        workflow = Workflow.Builder.aWorkflow()
                .name("TestWL")
                .ref("test_wl1")
                .description("测试流程1")
                .nodes(nodes)
                .build();
        WorkflowInstanceDomainService workflowInstanceDomainService = new WorkflowInstanceDomainService();
        instance = workflowInstanceDomainService.create("trigger567", "CRON", 1, workflow);
    }

    @Test
    @DisplayName("流程实例创建测试")
    void buildTest() {
        WorkflowInstance instance = workflowInstanceDomainService.create("trigger567", "CRON", 1, workflow);
        assertEquals(instance.getRunMode(), RunMode.AUTO);
        assertEquals(instance.getStatus(), ProcessStatus.RUNNING);
    }

    @Test
    @DisplayName("流程启动节点激活测试")
    void activateStartTest() {
        DomainEventSubscriber<NodeActivatingEvent> nodeActivatingEventDomainEventSubscriber = event -> {
            assertEquals(event.getWorkflowRef(), workflow.getRef());
            assertEquals(event.getWorkflowVersion(), workflow.getVersion());
            assertEquals(event.getName(), NodeActivatingEvent.class.getSimpleName());
            assertEquals(event.getWorkflowInstanceId(), instance.getId());
            assertEquals(event.getNodeRef(), "asyncTask_1");
        };
        DomainEventSubscriber<WorkflowStartEvent> workflowStartEventDomainEventSubscriber = event -> {
            assertEquals(event.getWorkflowRef(), workflow.getRef());
            assertEquals(event.getWorkflowVersion(), workflow.getVersion());
            assertEquals(event.getName(), WorkflowStartEvent.class.getSimpleName());
            assertEquals(event.getWorkflowInstanceId(), instance.getId());
            assertEquals(event.getNodeRef(), "start_1");
        };
        DomainEventPublisher.subscribe(NodeActivatingEvent.class, nodeActivatingEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(WorkflowStartEvent.class, workflowStartEventDomainEventSubscriber);

        workflowInstanceDomainService.activateNode(workflow, instance, workflow.findStart().getRef());
    }

    @Test
    @DisplayName("流程结束节点激活测试")
    void activateEndTest() {
        DomainEventSubscriber<WorkflowEndEvent> workflowEndEventDomainEventSubscriber = event -> {
            assertEquals(event.getWorkflowRef(), workflow.getRef());
            assertEquals(event.getWorkflowVersion(), workflow.getVersion());
            assertEquals(event.getName(), WorkflowEndEvent.class.getSimpleName());
            assertEquals(event.getWorkflowInstanceId(), instance.getId());
            assertEquals(event.getNodeRef(), "end_1");
        };
        DomainEventPublisher.subscribe(WorkflowEndEvent.class, workflowEndEventDomainEventSubscriber);
        workflowInstanceDomainService.activateNode(workflow, instance, "end_1");
    }

    @Test
    @DisplayName("任务节点激活测试")
    void activateTaskTest() {
        DomainEventSubscriber<TaskActivatingEvent> subscriber = event -> {
            AsyncTaskInstance taskInstance = instance.findInstanceByRef("asyncTask_1")
                    .orElseThrow(RuntimeException::new);
            assertEquals(event.getWorkflowRef(), workflow.getRef());
            assertEquals(event.getWorkflowVersion(), workflow.getVersion());
            assertEquals(event.getName(), TaskActivatingEvent.class.getSimpleName());
            assertEquals(event.getWorkflowInstanceId(), instance.getId());
            assertEquals(event.getNodeRef(), "asyncTask_1");
        };
        DomainEventPublisher.subscribe(TaskActivatingEvent.class, subscriber);

        workflowInstanceDomainService.activateNode(workflow, instance, "asyncTask_1");
    }

    @Test
    @DisplayName("任务节点中止测试")
    void terminateTaskTest() {
        DomainEventSubscriber<TaskActivatingEvent> activatingEventDomainEventSubscriber = event -> {
            AsyncTaskInstance taskInstance = instance.findInstanceByRef("asyncTask_1")
                    .orElseThrow(RuntimeException::new);
            assertEquals(event.getWorkflowRef(), workflow.getRef());
            assertEquals(event.getWorkflowVersion(), workflow.getVersion());
            assertEquals(event.getName(), TaskActivatingEvent.class.getSimpleName());
            assertEquals(event.getWorkflowInstanceId(), instance.getId());
            assertEquals(event.getNodeRef(), "asyncTask_1");
        };
        DomainEventPublisher.subscribe(TaskActivatingEvent.class, activatingEventDomainEventSubscriber);

        DomainEventSubscriber<TaskTerminatingEvent> terminatingEventDomainEventSubscriber = event -> {
            AsyncTaskInstance taskInstance = instance.findInstanceByRef("asyncTask_1")
                    .orElseThrow(RuntimeException::new);
            assertEquals(event.getWorkflowInstanceId(), instance.getId());
            assertEquals(event.getWorkflowRef(), instance.getWorkflowRef());
            assertEquals(event.getWorkflowVersion(), instance.getWorkflowVersion());
            assertEquals(event.getNodeRef(), "asyncTask_1");
            assertEquals(event.getName(), TaskTerminatingEvent.class.getSimpleName());
        };
        DomainEventPublisher.subscribe(TaskTerminatingEvent.class, terminatingEventDomainEventSubscriber);

        workflowInstanceDomainService.activateNode(workflow, instance, "asyncTask_1");

        workflowInstanceDomainService.terminateNode(workflow, instance, "asyncTask_1");
    }
}
