package dev.jianmu.application;

import dev.jianmu.workflow.aggregate.definition.AsyncTask;
import dev.jianmu.workflow.aggregate.definition.Condition;
import dev.jianmu.workflow.aggregate.definition.End;
import dev.jianmu.workflow.aggregate.definition.Start;
import dev.jianmu.workflow.el.ExpressionLanguage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @class: WorkflowTest
 * @description: Workflow测试类
 * @author: Ethan Liu
 * @create: 2021-03-13 08:13
 **/
@SpringBootTest
@DisplayName("Workflow测试类")
public class WorkflowTest {
    @Resource
    private ExpressionLanguage expressionLanguage;
    private static Start start1;
    private static Start start2;
    private static End end1;
    private static End end2;
    private static Condition condition1;
    private static Condition condition2;
    private static AsyncTask asyncTask1;
    private static AsyncTask asyncTask2;

    @BeforeAll
    static void beforeAll() {
//        PrintEventSubscriber.sub();
        asyncTask1 = AsyncTask.Builder.anAsyncTask()
                .name("AsyncTask1")
                .ref("asyncTask_1")
                .description("异步任务1")
                .build();
        asyncTask2 = AsyncTask.Builder.anAsyncTask()
                .name("AsyncTask2")
                .ref("asyncTask_2")
                .description("异步任务2")
                .build();
        end1 = End.Builder.anEnd()
                .name("End1")
                .ref("end_1")
                .description("结束节点1")
                .build();
        end2 = End.Builder.anEnd()
                .name("End2")
                .ref("end_2")
                .description("结束节点2")
                .build();
        start1 = Start.Builder.aStart()
                .name("Start1")
                .ref("start_1")
                .description("开始节点1")
                .build();
        start1.setTargets(Set.of(end1.getRef()));
        start2 = Start.Builder.aStart()
                .name("Start2")
                .ref("start_2")
                .description("开始节点2")
                .build();
        start2.setTargets(Set.of(end2.getRef()));
        condition1 = Condition.Builder.aCondition()
                .ref("condition1")
                .expression("")
                .build();
        condition2 = Condition.Builder.aCondition()
                .ref("condition1").build();
    }

//    @Test
//    @DisplayName("激活节点方法测试1")
//    void activateNodeTest1() {
//        start1.setTargets(Set.of(asyncTask1, asyncTask2));
//        asyncTask1.setTargets(Set.of(start1));
//        asyncTask2.setTargets(Set.of(start1));
//        asyncTask1.setSources(Set.of(end1));
//        asyncTask2.setSources(Set.of(end1));
//        end1.setSources(Set.of(asyncTask1, asyncTask2));
//        Set<Node> nodes = Set.of(start1, asyncTask1, asyncTask2, end1);
//        Workflow workflow1 = Workflow.Builder.aWorkflow()
//                .name("TestWL")
//                .ref("test_wl1")
//                .version("1.0")
//                .description("测试流程1")
//                .nodes(nodes)
//                .build();
//        WorkflowInstance workflowInstance1 = WorkflowInstance.Builder.aWorkflowInstance()
//                .name(workflow1.getName())
//                .description(workflow1.getDescription())
//                .workflowRef(workflow1.getRef())
//                .workflowVersion(workflow1.getVersion())
//                .build();
//        Throwable exception = assertThrows(RuntimeException.class, () ->
//                workflow1.activateNode(workflowInstance1, "start_2")
//        );
//        assertEquals("未找到要激活的节点", exception.getMessage(), "未找到要激活的节点");
//    }
//
//    @Test
//    @DisplayName("激活节点方法测试2")
//    void activateNodeTest2() {
//        start1.setTargets(Set.of(asyncTask1, asyncTask2));
//        asyncTask1.setTargets(Set.of(start1));
//        asyncTask2.setTargets(Set.of(start1));
//        asyncTask1.setSources(Set.of(end1));
//        asyncTask2.setSources(Set.of(end1));
//        end1.setSources(Set.of(asyncTask1, asyncTask2));
//        Set<Node> nodes = Set.of(start1, asyncTask1, asyncTask2, end1);
//        Workflow workflow1 = Workflow.Builder.aWorkflow()
//                .name("TestWL")
//                .ref("test_wl1")
//                .version("1.0")
//                .description("测试流程1")
//                .nodes(nodes)
//                .build();
//        WorkflowInstance workflowInstance1 = WorkflowInstance.Builder.aWorkflowInstance()
//                .name(workflow1.getName())
//                .description(workflow1.getDescription())
//                .workflowRef(workflow1.getRef())
//                .workflowVersion(workflow1.getVersion())
//                .build();
//        DomainEventSubscriber<TaskActivatingEvent> subscriber = event -> {
//            assertEquals(event.getWorkflowInstanceId(), workflowInstance1.getId(), "激活事件中流程实例ID不一致");
//            assertEquals(event.getWorkflowRef(), workflow1.getRef(), "激活事件中流程唯一引用名称不一致");
//            assertEquals(event.getWorkflowVersion(), workflow1.getVersion(), "激活事件中流程版本不一致");
//            assertTrue((event.getNodeRef().equals(asyncTask1.getRef()) || event.getNodeRef().equals(asyncTask2.getRef())), "激活事件中节点事件与预期不符");
//            assertEquals(event.getName(), TaskActivatingEvent.class.getSimpleName(), "激活事件中事件名称与预期不符");
//        };
//        DomainEventPublisher.subscribe(TaskActivatingEvent.class, subscriber);
//        workflow1.activateNode(workflowInstance1, start1.getRef());
//    }
//
//    @Test
//    @DisplayName("激活节点方法测试3")
//    void activateNodeTest3() {
//        start1.setTargets(Set.of(condition1));
//        condition1.setSources(Set.of(start1));
//        condition1.setTargets(Set.of(asyncTask1, asyncTask2));
//        condition1.setTargetMap(Map.of(true, asyncTask1.getRef(), false, asyncTask2.getRef()));
//        asyncTask1.setTargets(Set.of(condition1));
//        asyncTask2.setTargets(Set.of(condition1));
//        asyncTask1.setSources(Set.of(end1));
//        asyncTask2.setSources(Set.of(end1));
//        end1.setSources(Set.of(asyncTask1, asyncTask2));
//        Set<Node> nodes = Set.of(start1, condition1, asyncTask1, asyncTask2, end1);
//        Workflow workflow1 = Workflow.Builder.aWorkflow()
//                .name("TestWL")
//                .ref("test_wl1")
//                .version("1.0")
//                .description("测试流程1")
//                .nodes(nodes)
//                .build();
//        WorkflowInstance workflowInstance1 = WorkflowInstance.Builder.aWorkflowInstance()
//                .name(workflow1.getName())
//                .description(workflow1.getDescription())
//                .workflowRef(workflow1.getRef())
//                .workflowVersion(workflow1.getVersion())
//                .build();
//        workflowInstance1.setExpressionLanguage(this.expressionLanguage);
//        DomainEventSubscriber<ConditionEvent> subscriber = event -> {
//            assertEquals(event.getWorkflowInstanceId(), workflowInstance1.getId(), "激活事件中流程实例ID不一致");
//            assertEquals(event.getWorkflowRef(), workflow1.getRef(), "激活事件中流程唯一引用名称不一致");
//            assertEquals(event.getWorkflowVersion(), workflow1.getVersion(), "激活事件中流程版本不一致");
//            assertEquals(event.getNodeRef(), condition1.getRef() , "激活事件中节点ref与预期不符");
//            assertEquals(event.getName(), ConditionEvent.class.getSimpleName(), "激活事件中事件名称与预期不符");
//            assertEquals(event.getTargetNodeRef(), asyncTask1.getRef(), "ConditionEvent事件目标Node Ref与预期不符");
//        };
//        DomainEventPublisher.subscribe(ConditionEvent.class, subscriber);
//        workflow1.activateNode(workflowInstance1, start1.getRef());
//    }
}
