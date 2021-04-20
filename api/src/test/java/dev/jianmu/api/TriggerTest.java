package dev.jianmu.api;

import dev.jianmu.application.service.ParameterApplication;
import dev.jianmu.application.service.TriggerApplication;
import dev.jianmu.trigger.aggregate.Trigger;
import dev.jianmu.trigger.aggregate.TriggerParameter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * @class: TriggerTest
 * @description: 触发器测试
 * @author: Ethan Liu
 * @create: 2021-04-12 08:43
 **/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("test")
public class TriggerTest {
    @Resource
    private TriggerApplication triggerApplication;
    @Resource
    private ParameterApplication parameterApplication;

    @Test
    @Transactional
    @Rollback(value = false)
    void test1() {
        String workflowId = "java_ci1.1";
        Set<TriggerParameter> triggerParameters = new HashSet<>();
        TriggerParameter gitUrl = TriggerParameter.Builder.aTriggerParameter()
                .name("Git Repo")
                .description("git库地址")
                .type("String")
                .value("https://gitee.com/jianmu_dev/jianmu-workflow-core.git")
                .ref("git_url")
                .build();
        triggerParameters.add(gitUrl);
        this.triggerApplication.addWorkflowTrigger(workflowId, Trigger.Type.EVENT, triggerParameters);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void test2() {
        String triggerId = "de576327fc774666add3476e09b8b528";
        String parameterId = "1271bfc6910f4d53b2d44837f3686832";
        Set<String> linkedParameterIds = new HashSet<>();
        linkedParameterIds.add("c66a1ed100724c5abbaf7fc95a2c927c");

        this.parameterApplication.addReferences(triggerId, parameterId, linkedParameterIds);
    }
}
