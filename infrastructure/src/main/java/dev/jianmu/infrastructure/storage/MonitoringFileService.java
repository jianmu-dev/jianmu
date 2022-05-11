package dev.jianmu.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
    private final WatchService watchService;
    private final Path monitoringDirectory;
    private final Map<String, Consumer<Path>> callbackMap = new ConcurrentHashMap<>();

    public MonitoringFileService(StorageProperties properties) throws IOException {
        var rootLocation = Paths.get("ci", properties.getLogfilePath());
        try {
            Files.createDirectories(rootLocation);
        } catch (FileAlreadyExistsException e) {
            log.info("the directory already exits");
        }
        this.monitoringDirectory = rootLocation;
        this.watchService = FileSystems.getDefault().newWatchService();
        var key = monitoringDirectory.register(watchService, ENTRY_MODIFY);
        System.out.println(key);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::monitor);
    }

    public void listen(String topic, Consumer<Path> consumer) {
        callbackMap.put(topic, consumer);
    }

    void monitor() {
        while (true) {
            try {
                var key = this.watchService.take();
                for (final WatchEvent<?> event : key.pollEvents()) {
                    final Path changed = monitoringDirectory.resolve((Path) event.context());
                    final String fileName = changed.getFileName().toString();
                    var callback = callbackMap.get(fileName);

                    if (event.kind() == ENTRY_MODIFY && callback != null) {
                        log.trace("monitor - ENTRY_MODIFY: " + changed);
                        callback.accept(changed);
                    }
                }

                boolean isKeyStillValid = key.reset();
                if (!isKeyStillValid) {
                    log.trace("monitor - key is no longer valid: " + key);
                }
            } catch (InterruptedException ex) {
                log.trace("");
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        this.watchService.close();
    }
}
