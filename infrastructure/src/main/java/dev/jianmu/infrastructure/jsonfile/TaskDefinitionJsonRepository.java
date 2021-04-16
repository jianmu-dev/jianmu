package dev.jianmu.infrastructure.jsonfile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.infrastructure.storage.StorageException;
import dev.jianmu.task.aggregate.TaskDefinition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.repository.TaskDefinitionRepository;
import dev.jianmu.workflow.aggregate.definition.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @class: TaskDefinitionJsonRepository
 * @description: 任务定义Json仓储实现类
 * @author: Ethan Liu
 * @create: 2021-04-15 16:08
 **/
@Repository
public class TaskDefinitionJsonRepository implements TaskDefinitionRepository, ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(TaskDefinitionJsonRepository.class);
    private final ObjectMapper objectMapper;
    private static final String POSTFIX = ".json";

    private final Path rootLocation;

    private void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (FileAlreadyExistsException e) {
            logger.info("the directory already exits");
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Inject
    public TaskDefinitionJsonRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.rootLocation = Path.of("task_def");
    }

    @Override
    public void add(TaskDefinition taskDefinition) {
        try {
            var writer = new FileWriter(
                    this.rootLocation +
                            File.separator +
                            taskDefinition.getKey() +
                            taskDefinition.getVersion() +
                            POSTFIX
            );
            this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            this.objectMapper.writerFor(new TypeReference<TaskDefinition>() {
            }).writeValue(writer, taskDefinition);
        } catch (IOException e) {
            logger.error("无法保存为Json文件", e);
            throw new RuntimeException("任务定义保存失败");
        }
    }

    @Override
    public Optional<TaskDefinition> findByKeyVersion(String keyVersion) {
        try {
            var writer = new FileReader(
                    this.rootLocation +
                            File.separator +
                            keyVersion +
                            POSTFIX
            );
            this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            SimpleModule module = new SimpleModule();
            Class type1 = Set.of(TaskParameter.Builder.aTaskParameter().build()).getClass();
            Class type2 = Set.of().getClass();
            module.addDeserializer(type1, new UnmodifiableSetDeserializer());
            module.addDeserializer(type2, new UnmodifiableSetDeserializer());
            this.objectMapper.registerModule(module);
            TaskDefinition taskDefinition = this.objectMapper.readValue(writer, TaskDefinition.class);
            return Optional.of(taskDefinition);
        } catch (IOException e) {
            logger.error("未找到该任务定义", e);
        }
        return Optional.empty();
    }

    @Override
    public List<TaskDefinition> findAll() {
        return null;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.init();
    }
}
