package dev.jianmu.infrastructure.storage;

import dev.jianmu.infrastructure.storage.vo.ConsumerVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * @author Ethan Liu
 * @class MonitoringFileService
 * @description MonitoringFileService
 * @create 2022-05-11 08:40
 */
@Service
@Slf4j
public class MonitoringFileService implements DisposableBean {
    private static Map<WatchKey, Path> keyPathMap = new ConcurrentHashMap<>();
    private final WatchService watchService;
    private final Path monitoringTaskDirectory;
    private final Path monitoringWorkflowDirectory;
    private final Map<String, CopyOnWriteArrayList<ConsumerVo>> callbackMap = new ConcurrentHashMap<>();

    public MonitoringFileService(StorageProperties properties) throws IOException {
        var taskRootLocation = Paths.get("ci", properties.getLogfilePath());
        var workflowRootLocation = Paths.get("ci", "workflow_log");
        try {
            Files.createDirectories(taskRootLocation);
            Files.createDirectories(workflowRootLocation);
        } catch (FileAlreadyExistsException e) {
            log.info("the directory already exits");
        }
        this.monitoringTaskDirectory = taskRootLocation;
        this.monitoringWorkflowDirectory = workflowRootLocation;
        this.watchService = FileSystems.getDefault().newWatchService();
        var taskKey = this.monitoringTaskDirectory.register(this.watchService, ENTRY_MODIFY);
        var workflowKey = this.monitoringWorkflowDirectory.register(this.watchService, ENTRY_MODIFY);
        keyPathMap.put(taskKey, this.monitoringTaskDirectory);
        keyPathMap.put(workflowKey, this.monitoringWorkflowDirectory);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::monitor);
    }

    public void listen(String topic, String connectionId, BiConsumer<Path, AtomicLong> consumer) {
        callbackMap.putIfAbsent(topic, new CopyOnWriteArrayList<>());
        callbackMap.get(topic).add(new ConsumerVo(connectionId, consumer));
    }

    void monitor() {
        while (true) {
            try {
                var key = this.watchService.take();
                for (final WatchEvent<?> event : key.pollEvents()) {
                    var path = keyPathMap.get(key);
                    final Path changed = path.resolve((Path) event.context());
                    final String fileName = changed.getFileName().toString();
                    var set = this.callbackMap.get(fileName);
                    if (event.kind() == ENTRY_MODIFY && set != null) {
                        set.forEach(consumerVo -> {
                            log.trace("monitor - ENTRY_MODIFY: " + changed);
                            consumerVo.getConsumer().accept(changed, consumerVo.getCounter());
                        });
                    }
                }
                boolean isKeyStillValid = key.reset();
                if (!isKeyStillValid) {
                    log.trace("monitor - key is no longer valid: " + key);
                    keyPathMap.remove(key);
                }
            } catch (Exception ex) {
                log.warn("MonitoringFileService:", ex);
                log.trace("");
            }
        }
    }

    public Path getPath(String topic) {
        var path = this.monitoringTaskDirectory.resolve(topic);
        if (!path.toFile().exists()) {
            path = this.monitoringWorkflowDirectory.resolve(topic);
        }
        return path;
    }

    public Optional<ConsumerVo> getConsumerVo(String topic, String connectionId) {
        return this.callbackMap.get(topic).stream()
                .filter(consumerVo -> consumerVo.getConnectionId().equals(connectionId))
                .findFirst();
    }

    public void removeConsumer(String connectionId) {
        String topic = connectionId.split("/")[0];
        var list = this.callbackMap.get(topic);
        if (list != null) {
            list.stream()
                    .filter(consumerVo -> consumerVo.getConnectionId().equals(connectionId))
                    .findFirst()
                    .ifPresent(list::remove);
            if (list.isEmpty()) {
                this.callbackMap.remove(topic);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        this.watchService.close();
    }
}
