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
    private static final String LogfilePostfix = ".log";

    private final Map<String, CopyOnWriteArrayList<ConsumerVo>> callbackMap = new ConcurrentHashMap<>();
    private WatchService watchService;
    private Path monitoringTaskDirectory;
    private Path monitoringWorkflowDirectory;

    public void init(Path taskPath, Path workflowPath) throws IOException {
        this.monitoringTaskDirectory = taskPath;
        this.monitoringWorkflowDirectory = workflowPath;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.monitoringWorkflowDirectory.register(this.watchService, ENTRY_MODIFY);
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
                    final Path changed = this.monitoringWorkflowDirectory.resolve((Path) event.context());
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
                }
            } catch (ClosedWatchServiceException ex) {
                log.trace("");
            } catch (Exception ex) {
                log.trace("MonitoringFileService:", ex);
            }
        }
    }

    public void clearTaskCallback(String logId) {
        var topic = logId + LogfilePostfix;
        this.callbackMap.remove(topic);
    }

    public void sendLog(String logId) {
        var topic = logId + LogfilePostfix;
        var list = this.callbackMap.get(topic);
        if (list == null) {
            return;
        }
        list.forEach(consumerVo -> {
            var path = this.monitoringTaskDirectory.resolve(topic);
            consumerVo.getConsumer().accept(path, consumerVo.getCounter());
        });
    }

    @Override
    public void destroy() throws Exception {
        this.watchService.close();
    }
}
