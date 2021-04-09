package dev.jianmu.api;

import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.aggregate.StringParameterDefinition;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import dev.jianmu.parameter.repository.ParameterInstanceRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.EnvType;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TaskDefinitionRepository taskDefinitionRepository;
    @Resource
    private ParameterDefinitionRepository parameterDefinitionRepository;
    @Resource
    private ParameterInstanceRepository parameterInstanceRepository;

    @Test
    void test4() {
        var taskDefinitions = this.parameterDefinitionRepository
                .findByBusinessIdAndScope("git_clone0.1", "TaskInput");
        var service = new ParameterDomainService();
        var list = service
                .createTaskInputParameterInstance("e8418a65c4d44ef0add5794e03c99346",taskDefinitions);
        list.forEach(instace -> {
            System.out.println(instace.getRef());
            System.out.println(instace.getValue());
        });
    }

    @Test
    void test3() {
        var workerDefinitions = this.parameterDefinitionRepository
                .findByBusinessIdAndScope("worker9527", "Worker");
        var taskInstance = this.parameterInstanceRepository
                .findByBusinessIdAndScope("e8418a65c4d44ef0add5794e03c99346", "TaskInput");
        var service = new ParameterDomainService();
        Map<String, String> systemParameterMap = service.mergeSystemParameterMap(workerDefinitions, taskInstance);
        Map<String, String> businessParameterMap = service.mergeBusinessParameterMap(workerDefinitions, taskInstance);
        System.out.println("系统参数如下：");
        systemParameterMap.forEach((key, val) -> {
            System.out.println("Map key: " + key);
            System.out.println("Map val: " + val);
        });
        System.out.println("业务参数如下：");
        businessParameterMap.forEach((key, val) -> {
            System.out.println("Map key: " + key);
            System.out.println("Map val: " + val);
        });
    }

    @Test
    @Transactional
    void test1() {
        TaskDefinition definition = TaskDefinition.Builder.aDefinition()
                .name("Git Clone")
                .description("Git库下载任务")
                .key("git_clone")
                .version("0.1")
                .envType(EnvType.CONTAINER)
                .build();
        StringParameterDefinition commit_branch = StringParameterDefinition.Builder.aStringDefinition()
                .name("Commit branch")
                .ref("commit_branch")
                .description("git commit branch name")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("master")
                .build();
        StringParameterDefinition netrc_machine = StringParameterDefinition.Builder.aStringDefinition()
                .name("Netrc Machine")
                .ref("netrc_machine")
                .description("Netrc Machine name")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("gitee.com")
                .build();
        StringParameterDefinition netrc_username = StringParameterDefinition.Builder.aStringDefinition()
                .name("Netrc Username")
                .ref("netrc_username")
                .description("Netrc Username")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("ethan-liu")
                .build();
        StringParameterDefinition netrc_password = StringParameterDefinition.Builder.aStringDefinition()
                .name("Netrc Password")
                .ref("netrc_password")
                .description("Netrc Password")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("314159gto")
                .build();
        StringParameterDefinition remote_url = StringParameterDefinition.Builder.aStringDefinition()
                .name("Repo")
                .ref("remote_url")
                .description("git repo url")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("https://gitee.com/libfintech/libpay_sdk.git")
                .build();
        StringParameterDefinition image = StringParameterDefinition.Builder.aStringDefinition()
                .name("Image")
                .ref("image")
                .description("Image name")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("jianmu/git:0.2")
                .build();
        StringParameterDefinition network = StringParameterDefinition.Builder.aStringDefinition()
                .name("Network")
                .ref("network")
                .description("Network name")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("bridge")
                .build();
        StringParameterDefinition workingDir = StringParameterDefinition.Builder.aStringDefinition()
                .name("WorkingDir")
                .ref("working_dir")
                .description("working_dir")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("/mydata")
                .build();
        StringParameterDefinition volume_mounts = StringParameterDefinition.Builder.aStringDefinition()
                .name("Volume Mounts")
                .ref("volume_mounts")
                .description("volume_mounts config")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("[{\"name\":\"vt2\",\"path\":\"/mydata\"}]")
                .build();
        StringParameterDefinition volumes = StringParameterDefinition.Builder.aStringDefinition()
                .name("Volumes")
                .ref("volumes")
                .description("Volumes config")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("[{\"temp\":{\"id\":\"v-test-2\",\"name\":\"vt2\"}}]")
                .build();
        this.taskDefinitionRepository.add(definition);
        this.parameterDefinitionRepository.addList(
                List.of(commit_branch, netrc_machine, netrc_username, netrc_password, remote_url,
                        image, network, workingDir, volume_mounts, volumes)
        );
    }

    @Test
    @Transactional
    void test2() {
        TaskDefinition definition = TaskDefinition.Builder.aDefinition()
                .name("Maven")
                .description("Maven命令执行环境")
                .key("maven")
                .version("0.1")
                .envType(EnvType.CONTAINER)
                .build();
        StringParameterDefinition image = StringParameterDefinition.Builder.aStringDefinition()
                .name("Image")
                .ref("image")
                .description("Image name")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("maven:3-jdk-8")
                .build();
        StringParameterDefinition network = StringParameterDefinition.Builder.aStringDefinition()
                .name("Network")
                .ref("network")
                .description("Network name")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("bridge")
                .build();
        StringParameterDefinition command = StringParameterDefinition.Builder.aStringDefinition()
                .name("Command")
                .ref("command")
                .description("Command list")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("[ \"pwd && ls -a && mvn package\" ]")
                .build();
        StringParameterDefinition entrypoint = StringParameterDefinition.Builder.aStringDefinition()
                .name("Entrypoint")
                .ref("entrypoint")
                .description("Entrypoint")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("[\"/bin/sh\",\"-c\"]")
                .build();
        StringParameterDefinition workingDir = StringParameterDefinition.Builder.aStringDefinition()
                .name("WorkingDir")
                .ref("working_dir")
                .description("working_dir")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("/mydata")
                .build();
        StringParameterDefinition volume_mounts = StringParameterDefinition.Builder.aStringDefinition()
                .name("Volume Mounts")
                .ref("volume_mounts")
                .description("volume_mounts config")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("[{\"name\":\"vt2\",\"path\":\"/mydata\"}]")
                .build();
        StringParameterDefinition volumes = StringParameterDefinition.Builder.aStringDefinition()
                .name("Volumes")
                .ref("volumes")
                .description("Volumes config")
                .scope("TaskInput")
                .businessId(definition.getKey() + definition.getVersion())
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("{\"volumes\":[{\"temp\":{\"id\":\"v-test-2\",\"name\":\"vt2\"}}]}")
                .build();
        this.taskDefinitionRepository.add(definition);
        this.parameterDefinitionRepository.addList(
                List.of(image, network, command, entrypoint, workingDir, volume_mounts, volumes)
        );
    }
}
