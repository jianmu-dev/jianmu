package dev.jianmu.api;

import dev.jianmu.application.service.TaskDefinitionApplication;
import dev.jianmu.task.aggregate.BaseTaskDefinition;
import dev.jianmu.task.aggregate.DockerTaskDefinition;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @class: TaskDefinitionTest
 * @description: 任务定义测试类
 * @author: Ethan Liu
 * @create: 2021-04-06 22:36
 **/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("test")
public class TaskDefinitionTest {

    @Resource
    private TaskDefinitionApplication taskDefinitionApplication;

    @Test
    void test4() {
        var definitionOptional = this.taskDefinitionApplication.findByKeyVersion("maven", "11");
        definitionOptional.ifPresent(taskDefinition -> {
            taskDefinition.getParameters().forEach(taskParameter -> {
                System.out.println(taskParameter.getRef());
                System.out.println(taskParameter.getParameterId());
            });
            System.out.println("---------------------");
            if (taskDefinition instanceof DockerTaskDefinition) {
                var image= ((DockerTaskDefinition) taskDefinition).getSpec().getImage();
                System.out.println(image);
            }
            System.out.println("---------------------");
        });
    }

    @Test
    void test3() {
        var definitionOptional = this.taskDefinitionApplication.findByKeyVersion("git_clone", "0.1");
        definitionOptional.ifPresent(taskDefinition -> {
            taskDefinition.getParameters().forEach(taskParameter -> {
                System.out.println(taskParameter.getRef());
                System.out.println(taskParameter.getParameterId());
            });
            System.out.println("---------------------");
            if (taskDefinition instanceof DockerTaskDefinition) {
                var image= ((DockerTaskDefinition) taskDefinition).getSpec().getImage();
                System.out.println(image);
            }
            System.out.println("---------------------");
        });
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void test1() {
        Set<TaskParameter> taskParameters = new HashSet<>();

        var spec = ContainerSpec.Builder.aContainerSpec()
                .image("jianmu/git:0.2")
                .build();

        TaskParameter commitBranch = TaskParameter.Builder.aTaskParameter()
                .name("Commit branch")
                .ref("commit_branch")
                .type("String")
                .description("git commit branch name")
                .value("master")
                .build();
        taskParameters.add(commitBranch);

        TaskParameter netrc_machine = TaskParameter.Builder.aTaskParameter()
                .name("Netrc Machine")
                .ref("netrc_machine")
                .type("String")
                .description("Netrc Machine name")
                .value("gitee.com")
                .build();
        taskParameters.add(netrc_machine);

        TaskParameter remote_url = TaskParameter.Builder.aTaskParameter()
                .name("Repo")
                .ref("remote_url")
                .type("String")
                .description("git repo url")
                .value("https://gitee.com/libfintech/libpay_sdk.git")
                .build();
        taskParameters.add(remote_url);

        TaskDefinition definition = DockerTaskDefinition.Builder.aDockerTaskDefinition()
                .name("Git Clone")
                .description("Git库下载任务")
                .key("git_clone")
                .version("0.1")
                .spec(spec)
                .build();
        definition.setParameters(taskParameters);

        this.taskDefinitionApplication.create(definition);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void test2() {
        Set<TaskParameter> taskParameters = new HashSet<>();

        var spec = ContainerSpec.Builder.aContainerSpec()
                .image("maven:3-jdk-8")
                .cmd(new String[]{"pwd && ls -a && mvn package"})
                .entrypoint(new String[]{"/bin/sh", "-c"})
                .build();

        TaskDefinition definition = DockerTaskDefinition.Builder.aDockerTaskDefinition()
                .name("Maven")
                .description("Maven命令执行环境")
                .key("maven")
                .version("0.1")
                .spec(spec)
                .build();
        definition.setParameters(taskParameters);

        this.taskDefinitionApplication.create(definition);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    void test5() {
        Set<TaskParameter> taskParameters = new HashSet<>();

        var spec = ContainerSpec.Builder.aContainerSpec()
                .image("maven:3-jdk-11")
                .cmd(new String[]{"pwd && ls -a && mvn package"})
                .entrypoint(new String[]{"/bin/sh", "-c"})
                .build();

        TaskDefinition definition = DockerTaskDefinition.Builder.aDockerTaskDefinition()
                .name("Maven")
                .description("Maven命令执行环境")
                .key("maven")
                .version("11")
                .spec(spec)
                .build();
        definition.setParameters(taskParameters);

        this.taskDefinitionApplication.create(definition);
    }
}
