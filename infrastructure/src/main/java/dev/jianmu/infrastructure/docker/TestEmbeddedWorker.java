package dev.jianmu.infrastructure.docker;

import dev.jianmu.embedded.worker.aggregate.EmbeddedWorker;
import dev.jianmu.embedded.worker.aggregate.EmbeddedWorkerTask;
import dev.jianmu.embedded.worker.aggregate.spec.ContainerSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.util.Map;

/**
 * @author Ethan Liu
 * @class TestDockerWorker
 * @description 测试TestDockerWorker
 * @create 2021-04-16 14:12
 */
@Service
@ConditionalOnProperty(prefix = "jianmu.worker", name = "type", havingValue = "DUMB", matchIfMissing = true)
public class TestEmbeddedWorker implements EmbeddedWorker {
    private static final Logger logger = LoggerFactory.getLogger(TestEmbeddedWorker.class);

    @Override
    public void createVolume(String volumeName, Map<String, ContainerSpec> specMap) {
        logger.info("createVolume: {}", volumeName);
    }

    @Override
    public void deleteVolume(String volumeName) {
        logger.info("deleteVolume: {}", volumeName);
    }

    @Override
    public void runTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {
        logger.info("runTask: {}", embeddedWorkerTask);
    }

    @Override
    public void resumeTask(EmbeddedWorkerTask embeddedWorkerTask, BufferedWriter logWriter) {
        logger.info("resumeTask: {}", embeddedWorkerTask);
    }

    @Override
    public void terminateTask(String triggerId, String taskInstanceId) {
        logger.info("terminateTask: {}", taskInstanceId);
    }

    @Override
    public void deleteImage(String imageName) {
        logger.info("deleteImage: {}", imageName);
    }

    @Override
    public void updateImage(String imageName) {
        logger.info("updateImage: {}", imageName);
    }
}
