package dev.jianmu.api;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.application.service.WorkflowInstanceApplication;
import dev.jianmu.application.service.internal.WorkflowInstanceInternalApplication;
import dev.jianmu.workflow.aggregate.definition.*;
import dev.jianmu.workflow.aggregate.process.WorkflowInstance;
import dev.jianmu.workflow.el.ExpressionLanguage;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
import dev.jianmu.workflow.service.WorkflowInstanceDomainService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @class dev.jianmu.web.WorkflowInstanceIntegrationTest
 * @description 流程实例集成测试类
 * @author Ethan Liu
 * @create 2021-03-21 19:35
*/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("test")
public class WorkflowTaskTriggerParameterInstanceIntegrationTest {
    @Resource
    private WorkflowInstanceRepository workflowInstanceRepository;
    @Resource
    private ExpressionLanguage expressionLanguage;
    @Resource
    private WorkflowInstanceInternalApplication workflowInstanceInternalApplication;

    private static final Logger logger = LoggerFactory.getLogger(WorkflowTaskTriggerParameterInstanceIntegrationTest.class);

    private static Workflow workflow;
    private static WorkflowInstance instance;

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
        WorkflowInstanceDomainService workflowInstanceDomainService = new WorkflowInstanceDomainService();
        instance = workflowInstanceDomainService.create("trigger567", "CRON", 1, workflow);
    }

//    @Test
//    @Transactional
//    void test1() {
//        Node node = workflow.findNode("asyncTask_1");
//        instance.activateNode(node);
//        this.workflowInstanceRepository.add(instance);
//    }
//
//    @Test
//    void test11() {
//        this.workflowInstanceInternalApplication.start("6e8840f303c949b09f3b50cb7ce88bad", "start_1");
//    }
//
//    @Test
//    @Transactional
//    void test12() {
//        Node node = workflow.findNode("condition_1");
//        instance.setExpressionLanguage(this.expressionLanguage);
//        instance.activateNode(node);
//        this.workflowInstanceRepository.add(instance);
//    }
//
//    @Test
//    void test2() {
//        Optional<WorkflowInstance> instanceOptional = this.workflowInstanceRepository.findById("6e8840f303c949b09f3b50cb7ce88bad");
//        instanceOptional.ifPresent(instance1 -> {
//            logger.info(instance1.getId());
//            logger.info(instance1.getName());
//            logger.info(instance1.getDescription());
//            instance1.getAsyncTaskInstances().forEach(task -> {
//                logger.info(task.getName());
//                logger.info(task.getDescription());
//            });
//        });
//    }
//
//    @Test
//    void test3() {
//        List<WorkflowInstance> instances = this.workflowInstanceRepository
//                .findByRefAndVersionAndStatus(instance.getWorkflowRef(), instance.getWorkflowVersion(), instance.getStatus());
//        instances.forEach(i -> {
//            System.out.println(i.getId());
//            System.out.println(i.getName());
//            System.out.println(i.getDescription());
//            i.getAsyncTaskInstances().forEach(task -> {
//                System.out.println(task.getName());
//                System.out.println(task.getDescription());
//            });
//        });
//    }
//
//    @Test
//    void test4() {
//        List<WorkflowInstance> instances = this.workflowInstanceRepository.findAll(1, 1);
//        instances.forEach(i -> {
//            System.out.println(i.getId());
//            System.out.println(i.getName());
//            System.out.println(i.getDescription());
//            i.getAsyncTaskInstances().forEach(task -> {
//                System.out.println(task.getName());
//                System.out.println(task.getDescription());
//            });
//        });
//    }

    @Test
    @Transactional
    void test5() {
        Optional<WorkflowInstance> instanceOptional = this.workflowInstanceRepository.findById("6e8840f303c949b09f3b50cb7ce88bad");
        WorkflowInstance workflowInstance = instanceOptional.orElseThrow(RuntimeException::new);
        this.workflowInstanceRepository.save(workflowInstance);
    }

    @Test
    @Transactional
    void threadTest() throws Exception {
        Optional<WorkflowInstance> instanceOptional = this.workflowInstanceRepository.findById("6e8840f303c949b09f3b50cb7ce88bad");
        WorkflowInstance workflowInstance = instanceOptional.orElseThrow(() -> new DataNotFoundException("找不到实例"));
        int clientTotal = 100;
        // 同时并发执行的线程数
        int threadTotal = 20;
        int count = 0;
        ExecutorService executorService = Executors.newCachedThreadPool();
        //信号量，此处用于控制并发的线程数
        final Semaphore semaphore = new Semaphore(threadTotal);
        //闭锁，可实现计数器递减
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal; i++) {
            executorService.execute(() -> {
                try {
                    //执行此方法用于获取执行许可，当总计未释放的许可数不超过200时，
                    //允许通行，否则线程阻塞等待，直到获取到许可。
                    semaphore.acquire();
                    this.workflowInstanceRepository.save(workflowInstance);
                    //释放许可
                    semaphore.release();
                } catch (Exception e) {
                    //log.error("exception", e);
                    e.printStackTrace();

                }
                //闭锁减一
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();//线程阻塞，直到闭锁值为0时，阻塞才释放，继续往下执行
        executorService.shutdown();
    }
}
