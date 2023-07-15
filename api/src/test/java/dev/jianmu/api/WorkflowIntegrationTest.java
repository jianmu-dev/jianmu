package dev.jianmu.api;

import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.repository.WorkflowRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * @class dev.jianmu.web.WorkflowIntegrationTest
 * @description 流程集成测试类
 * @author Ethan Liu
 * @create 2021-03-21 12:26
*/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("test")
public class WorkflowIntegrationTest {

    @Resource
    private WorkflowRepository workflowRepository;

    private static Workflow workflow;

    @BeforeAll
    static void beforeAll() {
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
        AsyncTask asyncTask2 = AsyncTask.Builder.anAsyncTask()
                .name("AsyncTask2")
                .ref("asyncTask_2")
                .description("异步任务节点2")
                .build();
        AsyncTask asyncTask3 = AsyncTask.Builder.anAsyncTask()
                .name("AsyncTask3")
                .ref("asyncTask_3")
                .description("异步任务节点3")
                .build();
        var b1 = Branch.Builder.aBranch()
                .matchedCondition(true)
                .target(asyncTask1.getRef())
                .build();
        var b2 = Branch.Builder.aBranch()
                .matchedCondition(false)
                .target(asyncTask2.getRef())
                .build();
        List<Branch> branches = List.of(b1, b2);
        Condition condition = Condition.Builder.aCondition()
                .name("Condition1")
                .ref("condition_1")
                .description("条件网关1")
                .branches(branches)
                .expression("1+1==2")
                .build();
        End end = End.Builder.anEnd()
                .name("End1")
                .ref("end_1")
                .description("结束节点1")
                .build();

        start.setTargets(Set.of(condition.getRef()));

        condition.setSources(Set.of(start.getRef()));
        condition.setTargets(Set.of(asyncTask1.getRef(), asyncTask2.getRef()));

        asyncTask1.setSources(Set.of(condition.getRef()));
        asyncTask1.setTargets(Set.of(end.getRef()));

        asyncTask2.setSources(Set.of(condition.getRef()));
        asyncTask2.setTargets(Set.of(end.getRef()));

        asyncTask3.setSources(Set.of(asyncTask1.getRef()));
        asyncTask3.setTargets(Set.of(end.getRef()));

        end.setSources(Set.of(asyncTask3.getRef(), asyncTask2.getRef()));

        Set<Node> nodes = Set.of(start, condition, asyncTask1, asyncTask2, asyncTask3, end);
        workflow = Workflow.Builder.aWorkflow()
                .name("TestWL")
                .ref("test_wl1")
                .description("测试流程1")
                .nodes(nodes)
                .build();
    }

    @Test
    @Transactional
    void test1() {
        Workflow workflow1 = this.workflowRepository.add(workflow);
        System.out.println(workflow1.getName());
    }

    @Test
    void test2() {
        Optional<Workflow> workflowOptional = this.workflowRepository.findByRefAndVersion("test_wl1", "1.0");
        workflowOptional.ifPresent(workflow -> {
            System.out.println(workflow.getName());
            System.out.println(workflow.getDescription());
            System.out.println(workflow.getRef());
            System.out.println(workflow.getVersion());
            workflow.getNodes().forEach(node -> System.out.println(node.getName()));
        });
    }

    @Test
    @Transactional
    void test3() {
        this.workflowRepository.deleteByRefAndVersion("test_wl1", "1.0");
    }
}
