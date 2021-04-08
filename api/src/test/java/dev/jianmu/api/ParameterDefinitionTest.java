package dev.jianmu.api;

import dev.jianmu.parameter.aggregate.ParameterDefinition;
import dev.jianmu.parameter.aggregate.StringParameterDefinition;
import dev.jianmu.parameter.repository.ParameterDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @class: ParameterDefinitionTest
 * @description: 参数定义测试类
 * @author: Ethan Liu
 * @create: 2021-04-07 09:18
 **/
@SpringBootTest(classes = SpringbootApp.class)
@ActiveProfiles("test")
public class ParameterDefinitionTest {
    @Resource
    private ParameterDefinitionRepository parameterDefinitionRepository;

    @Test
    @Transactional
    void test1() {
        StringParameterDefinition image = StringParameterDefinition.Builder.aStringDefinition()
                .name("Image")
                .ref("image")
                .description("Image name")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("")
                .build();
        StringParameterDefinition network = StringParameterDefinition.Builder.aStringDefinition()
                .name("Network")
                .ref("network")
                .description("Network name")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("bridge")
                .build();
        StringParameterDefinition command = StringParameterDefinition.Builder.aStringDefinition()
                .name("Command")
                .ref("command")
                .description("Command list")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("")
                .build();
        StringParameterDefinition entrypoint = StringParameterDefinition.Builder.aStringDefinition()
                .name("Entrypoint")
                .ref("entrypoint")
                .description("Entrypoint")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("")
                .build();
        StringParameterDefinition workingDir = StringParameterDefinition.Builder.aStringDefinition()
                .name("WorkingDir")
                .ref("working_dir")
                .description("working_dir")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("")
                .build();
        StringParameterDefinition volume_mounts = StringParameterDefinition.Builder.aStringDefinition()
                .name("Volume Mounts")
                .ref("volume_mounts")
                .description("volume_mounts config")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("")
                .build();
        StringParameterDefinition volumes = StringParameterDefinition.Builder.aStringDefinition()
                .name("Volumes")
                .ref("volumes")
                .description("Volumes config")
                .scope("Worker")
                .businessId("worker9527")
                .source(ParameterDefinition.Source.INTERNAL)
                .type(ParameterDefinition.Type.IMMUTABLE)
                .value("{\"volumes\":[{\"temp\":{\"id\":\"v-test-2\",\"name\":\"vt2\"}}]}")
                .build();
        this.parameterDefinitionRepository.addList(List.of(image, network, command, entrypoint, workingDir, volume_mounts, volumes));
    }
}
