package dev.jianmu.infrastructure.storage;

import dev.jianmu.infrastructure.storage.vo.ConsumerVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
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
    private static final Map<WatchKey, Path> keyPathMap = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<ConsumerVo>> callbackMap = new ConcurrentHashMap<>();
    private WatchService watchService;
    private Path monitoringTaskDirectory;
    private Path monitoringWorkflowDirectory;

    public void init(Path taskPath, Path workflowPath) throws IOException {
        this.monitoringTaskDirectory = taskPath;
        this.monitoringWorkflowDirectory = workflowPath;
        this.watchService = FileSystems.getDefault().newWatchService();
        var taskKey = this.monitoringTaskDirectory.register(this.watchService, ENTRY_MODIFY);
        var workflowKey = this.monitoringWorkflowDirectory.register(this.watchService, ENTRY_MODIFY);
        keyPathMap.put(taskKey, this.monitoringTaskDirectory);
        keyPathMap.put(workflowKey, this.monitoringWorkflowDirectory);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::monitor);
    }

    public ConsumerVo listen(String topic, BiConsumer<Path, AtomicLong> consumer) {
        callbackMap.putIfAbsent(topic, new CopyOnWriteArrayList<>());
        var consumerVo = new ConsumerVo(consumer);
        callbackMap.get(topic).add(consumerVo);
        return consumerVo;
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
            } catch (ClosedWatchServiceException ex) {
                log.trace("");
            } catch (Exception ex) {
                log.trace("MonitoringFileService:", ex);
            }
        }
    }

    public String getFilePath(String topic) {
        var path = this.monitoringTaskDirectory.resolve(topic);
        if (!path.toFile().exists()) {
            path = this.monitoringWorkflowDirectory.resolve(topic);
        }
        return path.toFile().getPath();
    }

    public void clearTaskCallback(String topic) {
        this.callbackMap.remove(topic);
    }

    @Override
    public void destroy() throws Exception {
        this.watchService.close();
    }
}
