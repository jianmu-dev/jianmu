package dev.jianmu.infrastructure.storage;

import dev.jianmu.infrastructure.SseTemplate;
import dev.jianmu.infrastructure.storage.vo.ConsumerVo;
import dev.jianmu.infrastructure.storage.vo.LogVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ethan Liu
 * @class FileSystemStorageService
 * @description 文件系统实现类
 * @create 2021-04-05 21:01
 */
@Service
public class FileSystemStorageService implements StorageService, ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
    private static final String LogfilePostfix = ".log";
    private static final String webhookFilePostfix = ".json";
    private static final String taskFilepath = "task_log";
    private static final String workflowFilepath = "workflow_log";
    private static final String webhookFilepath = "webhook";
    // For SSE
    private final SseTemplate template;
    private final MonitoringFileService monitoringFileService;
    private final Path rootLocation;
    private final Path webhookRootLocation;
    private final Path workflowLocation;

    public FileSystemStorageService(SseTemplate template, MonitoringFileService monitoringFileService, StorageProperties properties) {
        this.template = template;
        this.monitoringFileService = monitoringFileService;
        this.rootLocation = Paths.get(properties.getFilepath(), taskFilepath);
        this.webhookRootLocation = Paths.get(properties.getFilepath(), webhookFilepath);
        this.workflowLocation = Paths.get(properties.getFilepath(), workflowFilepath);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.init();
        this.monitoringFileService.init(this.rootLocation, this.workflowLocation);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(this.rootLocation);
            Files.createDirectories(this.webhookRootLocation);
            Files.createDirectories(this.workflowLocation);
        } catch (FileAlreadyExistsException e) {
            logger.info("the directory already exits");
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public BufferedWriter writeLog(String LogFileName) {
        try {
            return new BufferedWriter(
                    new FileWriter(this.rootLocation + File.separator + LogFileName + LogfilePostfix, StandardCharsets.UTF_8, true)
            );
        } catch (IOException e) {
            throw new StorageException("Could not create log file", e);
        }
    }

    @Override
    public SseEmitter readLog(String logFileName, int size, boolean isTask) {
        var fullName = logFileName + LogfilePostfix;
        var sseEmitter = this.template.newSseEmitter();
        var consumerVo = this.monitoringFileService.listen(fullName, ((file, counter) -> {
            var endLine = this.countFileLines(file.toFile().getPath());
            var cmd = "sed -n '" + counter.intValue() + ", " + endLine + "p' " + file.toFile().getPath();
            try (var reader = this.execCmd(cmd)) {
                String str;
                while ((str = reader.readLine()) != null) {
                    this.template.sendMessage(SseEmitter.event()
                            .id(String.valueOf(counter.incrementAndGet()))
                            .data(str), sseEmitter);
                }
            } catch (IOException e) {
                throw new StorageException("Could not read log file", e);
            }
        }));
        String filePath = (isTask ? this.rootLocation : this.workflowLocation) + File.separator + fullName;
        this.firstReadLog(filePath, consumerVo, sseEmitter, size);
        return sseEmitter;
    }

    private int countFileLines(String filepath) {
        var cmd = "wc -l " + filepath + " | awk '{print $1}'";
        try (var reader = this.execCmd(cmd)) {
            var result = reader.readLine();
            if (result == null) {
                throw new StorageException("Could not to get file lines: " + filepath);
            }
            return Integer.parseInt(result);
        } catch (IOException e) {
            throw new StorageException("Could not to get execution result", e);
        }
    }

    private BufferedReader execCmd(String cmd) {
        try {
            var process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
            return new BufferedReader(new InputStreamReader(new BufferedInputStream(process.getInputStream())));
        } catch (IOException e) {
            throw new StorageException("Could not execution command", e);
        }
    }

    private void firstReadLog(String filePath, ConsumerVo consumerVo, SseEmitter sseEmitter, int size) {
        var endLine = this.countFileLines(filePath);
        var startLine = endLine > size ? endLine - size : 1;
        var cmd = "sed -n '" + startLine + ", " + (endLine - 1) + "p' " + filePath;
        try (var reader = this.execCmd(cmd)) {
            String str;
            while ((str = reader.readLine()) != null) {
                this.template.sendMessage(SseEmitter.event()
                        .id(String.valueOf(startLine++))
                        .data(str), sseEmitter);
            }
            consumerVo.getCounter().set(endLine);
        } catch (IOException e) {
            throw new StorageException("Could not read log file", e);
        }
    }

    @Override
    public List<LogVo> randomReadLog(String logFileName, Integer line, Integer size, boolean isTask) {
        var filePath = (isTask ? this.rootLocation : this.workflowLocation) + File.separator + logFileName + LogfilePostfix;
        var cmd = "sed -n '" + line + ", " + (line + size - 1) + "p' " + filePath;
        var list = new ArrayList<LogVo>();
        try (var reader = this.execCmd(cmd)) {
            String str;
            while ((str = reader.readLine()) != null) {
                list.add(LogVo.builder()
                        .lastEventId(String.valueOf(line++))
                        .data(str)
                .build());
            }
        } catch (IOException e) {
            throw new StorageException("Could not read log file", e);
        }
        return list;
    }

    @Override
    public File logFile(String LogFileName) {
        return new File(this.rootLocation + File.separator + LogFileName + LogfilePostfix);
    }

    @Override
    public File workflowLogFile(String LogFileName) {
        return new File(this.workflowLocation + File.separator + LogFileName + LogfilePostfix);
    }

    @Override
    public BufferedWriter writeWebhook(String webhookFileName) {
        try {
            return new BufferedWriter(
                    new FileWriter(this.webhookRootLocation + File.separator + webhookFileName + webhookFilePostfix, StandardCharsets.UTF_8, true)
            );
        } catch (IOException e) {
            throw new StorageException("Could not create webhook file", e);
        }
    }

    @Override
    public String readWebhook(String webhookFileName) {
        try {
            var bufferedReader = new BufferedReader(new FileReader(this.webhookRootLocation + File.separator + webhookFileName + webhookFilePostfix, StandardCharsets.UTF_8));
            var stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            logger.warn("获取webhook文件异常: {}", e.getMessage());
            throw new RuntimeException("webhook文件不存在");
        }
    }
}
