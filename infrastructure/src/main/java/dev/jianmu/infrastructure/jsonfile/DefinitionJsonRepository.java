package dev.jianmu.infrastructure.jsonfile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.jianmu.infrastructure.exception.DBException;
import dev.jianmu.infrastructure.jackson2.UnmodifiableSetDeserializer;
import dev.jianmu.task.aggregate.Definition;
import dev.jianmu.task.aggregate.TaskParameter;
import dev.jianmu.task.repository.DefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * @class: TaskDefinitionJsonRepository
 * @description: 任务定义Json仓储实现类
 * @author: Ethan Liu
 * @create: 2021-04-15 16:08
 **/
@Repository
public class DefinitionJsonRepository implements DefinitionRepository {
    private static final Logger logger = LoggerFactory.getLogger(DefinitionJsonRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonRepositoryInit init;
    private final ApplicationEventPublisher publisher;

    @Inject
    public DefinitionJsonRepository(JsonRepositoryInit init, ApplicationEventPublisher publisher) {
        this.init = init;
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRollback(final RollbackEvent event) {
        var file = new File(event.getFileName());
        if (!file.delete()) {
            throw new DBException.CleanupFailed("Rollback Clean up failed");
        }
    }

    @Override
    public void add(Definition definition) {
        var fileName = init.getRootLocation() +
                File.separator +
                definition.getResultFile() +
                definition.getVersion() +
                JsonRepositoryInit.POSTFIX;
        var event = new RollbackEvent();
        event.setFileName(fileName);
        try {
            var writer = new FileWriter(fileName, StandardCharsets.UTF_8);
            this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            this.objectMapper.writerFor(new TypeReference<Definition>() {
            }).writeValue(writer, definition);
        } catch (IOException e) {
            logger.error("无法保存为Json文件", e);
            throw new DBException.InsertFailed("任务定义保存失败");
        }
        publisher.publishEvent(event);
    }

    @Override
    public Optional<Definition> findByRefAndVersion(String refVersion) {
        try {
            var writer = new FileReader(
                    init.getRootLocation() +
                            File.separator +
                            refVersion +
                            JsonRepositoryInit.POSTFIX, StandardCharsets.UTF_8
            );
            this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            SimpleModule module = new SimpleModule();
            Class type1 = Set.of(TaskParameter.Builder.aTaskParameter().build()).getClass();
            Class type2 = Set.of().getClass();
            module.addDeserializer(type1, new UnmodifiableSetDeserializer());
            module.addDeserializer(type2, new UnmodifiableSetDeserializer());
            this.objectMapper.registerModule(module);
            Definition definition = this.objectMapper.readValue(writer, Definition.class);
            return Optional.of(definition);
        } catch (IOException e) {
            logger.error("未找到该任务定义", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Definition> findByRefAndVersion(String ref, String version) {
        try {
            var writer = new FileReader(
                    init.getRootLocation() +
                            File.separator +
                            ref +
                            version +
                            JsonRepositoryInit.POSTFIX, StandardCharsets.UTF_8
            );
            this.objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
            SimpleModule module = new SimpleModule();
            Class type1 = Set.of(TaskParameter.Builder.aTaskParameter().build()).getClass();
            Class type2 = Set.of().getClass();
            module.addDeserializer(type1, new UnmodifiableSetDeserializer());
            module.addDeserializer(type2, new UnmodifiableSetDeserializer());
            this.objectMapper.registerModule(module);
            Definition definition = this.objectMapper.readValue(writer, Definition.class);
            return Optional.of(definition);
        } catch (IOException e) {
            logger.error("未找到该任务定义", e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String key) {
        var file = new File(
                init.getRootLocation() +
                        File.separator +
                        key +
                        JsonRepositoryInit.POSTFIX
        );
        if (!file.delete()) {
            throw new DBException.DeleteFailed("任务定义删除失败");
        }
    }
}
