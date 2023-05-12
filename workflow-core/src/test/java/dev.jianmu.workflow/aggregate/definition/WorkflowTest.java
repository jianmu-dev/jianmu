package dev.jianmu.workflow.aggregate.definition;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @program: workflow
 * @description Workflow测试类
 * @author Ethan Liu
 * @create 2021-01-22 11:53
*/
@DisplayName("Workflow domain测试类")
public class WorkflowTest {
    private static Start start1;
    private static Start start2;
    private static End end1;
    private static End end2;
    private static Condition condition1;
    private static Condition condition2;

    @BeforeAll
    static void beforeAll() {
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

    @Test
    @DisplayName("Node数量不能小于2")
    void buildRuleTest1() {
        // 验证Node数量不能小于2
        Set<Node> nodes = Set.of(start1);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            Workflow.Builder.aWorkflow()
                    .name("TestWL")
                    .ref("test_wl1")
                    .description("测试流程1")
                    .nodes(nodes)
                    .build();
        });
        assertEquals("Node数量不能小于2", exception.getMessage(), "Node数量不能小于2");
    }

    @Test
    @DisplayName("开始节点不能多于1个")
    void buildRuleTest2() {
        // 开始节点不能多于1个
        Set<Node> nodes = Set.of(start1, end1, start2);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            Workflow.Builder.aWorkflow()
                    .name("TestWL")
                    .ref("test_wl1")
                    .description("测试流程1")
                    .nodes(nodes)
                    .build();
        });
        assertEquals("开始节点不存在或多于1个", exception.getMessage(), "开始节点不存在或多于1个");
    }

    @Test
    @DisplayName("开始节点不能没有")
    void buildRuleTest2_2() {
        // 开始节点不能没有
        Set<Node> nodes = Set.of(condition1, end1);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            Workflow.Builder.aWorkflow()
                .name("TestWL")
                .ref("test_wl1")
                .description("测试流程1")
                .nodes(nodes)
                .build();
        });
        assertEquals("开始节点不存在或多于1个", exception.getMessage(), "开始节点不存在或多于1个");
    }

    @Test
    @DisplayName("结束节点不能多于1个")
    void buildRuleTest3() {
        // 结束节点不能多于1个
        Set<Node> nodes = Set.of(start1, end1, end2);
        Throwable exception = assertThrows(RuntimeException.class, () ->
                Workflow.Builder.aWorkflow()
                        .name("TestWL")
                        .ref("test_wl1")
                        .description("测试流程1")
                        .nodes(nodes)
                        .build()
        );
        assertEquals("结束节点不存在或多于1个", exception.getMessage(), "结束节点不存在或多于1个");
    }

    @Test
    @DisplayName("结束节点不能没有")
    void buildRuleTest3_2() {
        // 结束节点不能没有
        Set<Node> nodes = Set.of(start1, condition1);
        Throwable exception = assertThrows(RuntimeException.class, () ->
            Workflow.Builder.aWorkflow()
                .name("TestWL")
                .ref("test_wl1")
                .description("测试流程1")
                .nodes(nodes)
                .build()
        );
        assertEquals("结束节点不存在或多于1个", exception.getMessage(), "结束节点不存在或多于1个");
    }

    @Test
    @DisplayName("节点唯一引用名称不允许重复")
    void buildRuleTest4() {
        // 节点唯一引用名称不允许重复
        Set<Node> nodes = Set.of(start1, condition1, condition2, end1);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            Workflow.Builder.aWorkflow()
                    .name("TestWL")
                    .ref("test_wl1")
                    .description("测试流程1")
                    .nodes(nodes)
                    .build();
        });
        assertEquals("节点唯一引用名称不允许重复", exception.getMessage(), "节点唯一引用名称不允许重复");
    }
}
