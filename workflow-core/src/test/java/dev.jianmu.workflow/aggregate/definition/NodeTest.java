package dev.jianmu.workflow.aggregate.definition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @program: workflow
 * @description 节点测试类
 * @author Ethan Liu
 * @create 2021-01-22 12:50
*/
@DisplayName("节点测试类")
public class NodeTest {
    private static Node testNode1;
    private static Node testNode2;
    private static Node testNode3;

    @BeforeAll
    static void beforeAll() {
        testNode1 = AsyncTask.Builder.anAsyncTask()
                .name("testNode1")
                .ref("test_node_1")
                .build();
        testNode2 = AsyncTask.Builder.anAsyncTask()
                .name("testNode2")
                .ref("test_node_2")
                .build();
        testNode3 = AsyncTask.Builder.anAsyncTask()
                .name("testNode3")
                .ref("test_node_3")
                .build();
    }

    @Test()
    @DisplayName("开始节点测试")
    void startTest() {
        Start start = Start.Builder.aStart()
                .name("Start1")
                .ref("start_1")
                .description("开始节点1")
                .build();
        Set<String> sources = start.getSources();
        // 开始节点的sources永远为空Set并且不能添加
        assertEquals(sources.size(), 0);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> sources.add(start.getRef()));
    }

    @Test
    @DisplayName("结束节点测试")
    void endTest() {
        End end = End.Builder.anEnd()
                .name("End1")
                .ref("end_1")
                .description("结束节点1")
                .build();
        Set<String> targets = end.getTargets();
        // 结束节点的targets永远为空Set并且不能添加
        assertEquals(targets.size(), 0);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> targets.add(end.getRef()));
    }

    @Test
    @DisplayName("Condition节点测试1")
    void conditionTest1() {
        String exp = "1==1";
        var b1 = Branch.Builder.aBranch()
                .matchedCondition(true)
                .target(testNode1.getRef())
                .build();
        var b2 = Branch.Builder.aBranch()
                .matchedCondition(false)
                .target(testNode2.getRef())
                .build();
        List<Branch> branches = List.of(b1, b2);
        Condition condition = Condition.Builder.aCondition()
                .name("Condition1")
                .ref("condition_1")
                .description("Condition节点1")
                .expression(exp)
                .branches(branches)
                .build();
        assertEquals(condition.ref, "condition_1");
    }

    @Test
    @DisplayName("Condition节点测试2")
    void conditionTest2() {
        String exp = "1+1 == 2";
        Map<Boolean, String> targetMap = Map.of(true, testNode1.getRef());
        var b1 = Branch.Builder.aBranch()
                .matchedCondition(true)
                .target(testNode1.getRef())
                .build();
        List<Branch> branches = List.of(b1);
        Condition condition = Condition.Builder.aCondition()
                .name("Condition1")
                .ref("condition_1")
                .description("Condition节点1")
                .expression(exp)
                .branches(branches)
                .build();
        Throwable exception = Assertions.assertThrows(RuntimeException.class, () -> condition.setTargets(Set.of(testNode1.getRef(),testNode2.getRef(),testNode3.getRef())));
        assertEquals("条件网关下游节点不得超过2个",exception.getMessage());
    }

    @Test
    @DisplayName("Switch节点测试1")
    void switchGatewayTest1() {
        String exp = "node1";
        Map<String, String> cases = Map.of("node1", testNode1.getRef(), "node2", testNode2.getRef());
        SwitchGateway switchGateway = SwitchGateway.Builder.aSwitchGateway()
                .name("Switch1")
                .ref("switch_1")
                .description("Switch节点1")
                .expression(exp)
                .cases(cases)
                .build();
        assertEquals(switchGateway.getRef(), "switch_1");
    }
}
