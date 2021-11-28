package dev.jianmu.workflow.aggregate.process;

import dev.jianmu.workflow.PrintEventSubscriber;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.event.DomainEventPublisher;
import dev.jianmu.workflow.event.DomainEventSubscriber;
import dev.jianmu.workflow.event.TaskFailedEvent;
import dev.jianmu.workflow.event.TaskRunningEvent;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @program: workflow
 * @description AsyncTask测试类
 * @author Ethan Liu
 * @create 2021-01-22 16:43
*/
@DisplayName("AsyncTask测试类")
public class AsyncTaskTest {
    private static AsyncTask asyncTask;
    private static WorkflowInstance workflowInstance;
    private static WorkflowInstanceDomainService workflowInstanceDomainService = new WorkflowInstanceDomainService();

    @BeforeAll
    static void beforeAll() {
        PrintEventSubscriber.sub();
        asyncTask = AsyncTask.Builder.anAsyncTask()
                .name("AsyncTask1")
                .ref("asyncTask_1")
                .type("AsyncTask1")
                .description("异步任务节点1")
                .build();
        Start start = Start.Builder.aStart()
                .name("Start1")
                .ref("start_1")
                .description("开始节点1")
                .build();
        End end = End.Builder.anEnd()
                .name("End1")
                .ref("end_1")
                .description("结束节点1")
                .build();
        start.setTargets(Set.of(asyncTask.getRef()));
        asyncTask.setSources(Set.of(start.getRef()));
        asyncTask.setTargets(Set.of(end.getRef()));
        end.setSources(Set.of(asyncTask.getRef()));

        Set<Node> nodes = Set.of(start, asyncTask, end);
        Workflow workflow = Workflow.Builder.aWorkflow()
                .name("TestWL")
                .ref("test_wl1")
                .description("测试流程1")
                .nodes(nodes)
                .build();
        workflowInstance = workflowInstanceDomainService.create("trigger567", "CRON", 1, workflow);
        workflowInstanceDomainService.activateNode(workflow, workflowInstance, "asyncTask_1");
    }

    @Test
    @DisplayName("异步任务开始执行测试")
    void taskRun() {
        DomainEventSubscriber<TaskRunningEvent> subscriber = event -> {
            assertEquals(event.getWorkflowInstanceId(), workflowInstance.getId());
            assertEquals(event.getWorkflowRef(), workflowInstance.getWorkflowRef());
            assertEquals(event.getWorkflowVersion(), workflowInstance.getWorkflowVersion());
            assertEquals(event.getNodeRef(), asyncTask.getRef());
            assertEquals(event.getName(), TaskRunningEvent.class.getSimpleName());
        };
        DomainEventPublisher.subscribe(TaskRunningEvent.class, subscriber);
        AsyncTaskInstance taskInstance = workflowInstance.findInstanceByRef(asyncTask.getRef()).orElseThrow(RuntimeException::new);
        assertEquals(taskInstance.getStatus(), TaskStatus.RUNNING);
    }

//    @Test
//    @DisplayName("异步任务执行成功测试")
//    void taskSucceed() {
//        DomainEventSubscriber<TaskSucceededEvent> subscriber = event -> {
//            assertEquals(event.getWorkflowInstanceId(), workflowInstance.getId());
//            assertEquals(event.getWorkflowRef(), workflowInstance.getWorkflowRef());
//            assertEquals(event.getWorkflowVersion(), workflowInstance.getWorkflowVersion());
//            assertEquals(event.getNodeRef(), asyncTask.getRef());
//            assertEquals(event.getName(), TaskSucceededEvent.class.getSimpleName());
//        };
//        DomainEventPublisher.subscribe(TaskSucceededEvent.class, subscriber);
//        AsyncTaskInstance taskInstance = workflowInstance.findInstanceByRef(asyncTask.getRef()).orElseThrow(RuntimeException::new);
//        workflowInstance.taskSucceed(taskInstance.getAsyncTaskRef());
//        assertEquals(taskInstance.getStatus(), TaskStatus.SUCCEEDED);
//    }

    @Test
    @DisplayName("异步任务执行失败测试")
    void taskFail() {
        DomainEventSubscriber<TaskFailedEvent> subscriber = event -> {
            assertEquals(event.getWorkflowInstanceId(), workflowInstance.getId());
            assertEquals(event.getWorkflowRef(), workflowInstance.getWorkflowRef());
            assertEquals(event.getWorkflowVersion(), workflowInstance.getWorkflowVersion());
            assertEquals(event.getNodeRef(), asyncTask.getRef());
            assertEquals(event.getName(), TaskFailedEvent.class.getSimpleName());
        };
        DomainEventPublisher.subscribe(TaskFailedEvent.class, subscriber);
        AsyncTaskInstance taskInstance = workflowInstance.findInstanceByRef(asyncTask.getRef()).orElseThrow(RuntimeException::new);
        workflowInstance.taskFail(taskInstance.getAsyncTaskRef());
        assertEquals(taskInstance.getStatus(), TaskStatus.FAILED);
    }
}
