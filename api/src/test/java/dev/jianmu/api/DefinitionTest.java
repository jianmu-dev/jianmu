package dev.jianmu.api;

import dev.jianmu.application.service.TaskDefinitionApplication;
import dev.jianmu.task.aggregate.DockerDefinition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.aggregate.spec.ContainerSpec;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * @class: TaskDefinitionTest
 * @description: 任务定义测试类
 * @author: Ethan Liu
 * @create: 2021-04-06 22:36
 **/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("dev")
public class DefinitionTest {

    @Resource
    private TaskDefinitionApplication taskDefinitionApplication;

    @Test
    void test4() {
        var taskDefinitionOptional = this.taskDefinitionApplication.findByRef("maven");
        taskDefinitionOptional.ifPresent(taskDefinition -> System.out.println(taskDefinition.getName()));
        var versions = this.taskDefinitionApplication.findVersionByRef("maven");
        versions.forEach(taskDefinitionVersion -> {
            System.out.println(taskDefinitionVersion.getName());
            System.out.println(taskDefinitionVersion.getTaskDefinitionRef());
            System.out.println(taskDefinitionVersion.getDefinitionKey());
        });
        var definitionOptional = this.taskDefinitionApplication.findByKey("maven11");
        definitionOptional.ifPresent(taskDefinition -> {
            taskDefinition.getParameters().forEach(taskParameter -> {
                System.out.println(taskParameter.getRef());
                System.out.println(taskParameter.getParameterId());
            });
            System.out.println("---------------------");
            if (taskDefinition instanceof DockerDefinition) {
                var image = ((DockerDefinition) taskDefinition).getSpec().getImage();
                System.out.println(image);
            }
            System.out.println("---------------------");
        });
    }

    @Test
    void test3() {
        var taskDefinitionOptional = this.taskDefinitionApplication.findByRef("git_clone");
        taskDefinitionOptional.ifPresent(taskDefinition -> System.out.println(taskDefinition.getName()));
        var versions = this.taskDefinitionApplication.findVersionByRef("git_clone");
        versions.forEach(taskDefinitionVersion -> {
            System.out.println(taskDefinitionVersion.getName());
            System.out.println(taskDefinitionVersion.getTaskDefinitionRef());
            System.out.println(taskDefinitionVersion.getDefinitionKey());
        });
        var definitionOptional = this.taskDefinitionApplication.findByKey("git_clone0.3");
        definitionOptional.ifPresent(taskDefinition -> {
            taskDefinition.getParameters().forEach(taskParameter -> {
                System.out.println(taskParameter.getRef());
                System.out.println(taskParameter.getParameterId());
            });
            System.out.println("---------------------");
            if (taskDefinition instanceof DockerDefinition) {
                var image = ((DockerDefinition) taskDefinition).getSpec().getImage();
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

        this.taskDefinitionApplication.createDockerDefinition(
                "Git Clone",
                "git_clone",
                "0.3",
                "/etc/hostname",
                "Git Clone Task",
                taskParameters,
                spec
        );
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

        this.taskDefinitionApplication.createDockerDefinition(
                "Maven",
                "maven",
                "0.3",
                "/etc/hostname",
                "Maven 3 with Jdk 8",
                taskParameters,
                spec
        );
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

        this.taskDefinitionApplication.createDockerDefinitionVersion(
                "maven",
                "11",
                "/etc/hostname",
                "Maven 3 with Jdk 11",
                taskParameters,
                spec
        );
    }
}
