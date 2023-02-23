package dev.jianmu.infrastructure.storage;

import dev.jianmu.infrastructure.SseTemplate;
import dev.jianmu.infrastructure.storage.vo.LogVo;
import dev.jianmu.task.aggregate.InstanceStatus;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.workflow.aggregate.process.ProcessStatus;
import dev.jianmu.workflow.repository.WorkflowInstanceRepository;
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
import java.util.concurrent.atomic.AtomicLong;

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
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    public FileSystemStorageService(SseTemplate template,
                                    MonitoringFileService monitoringFileService,
                                    StorageProperties properties,
                                    WorkflowInstanceRepository workflowInstanceRepository,
                                    TaskInstanceRepository taskInstanceRepository
    ) {
        this.template = template;
        this.monitoringFileService = monitoringFileService;
        this.rootLocation = Paths.get(properties.getFilepath(), taskFilepath);
        this.webhookRootLocation = Paths.get(properties.getFilepath(), webhookFilepath);
        this.workflowLocation = Paths.get(properties.getFilepath(), workflowFilepath);
        this.workflowInstanceRepository = workflowInstanceRepository;
        this.taskInstanceRepository = taskInstanceRepository;
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
    public BufferedWriter writeLog(String LogFileName, boolean append) {
        try {
            return new BufferedWriter(
                    new FileWriter(this.rootLocation + File.separator + LogFileName + LogfilePostfix, StandardCharsets.UTF_8, append)
            );
        } catch (IOException e) {
            throw new StorageException("Could not create log file", e);
        }
    }

    @Override
    public SseEmitter readLog(String logFileName, int size, boolean isTask) {
        boolean isComplete;
        if (isTask) {
            isComplete = this.taskInstanceRepository.findById(logFileName)
                    .stream()
                    .noneMatch(taskInstance ->
                            taskInstance.getStatus() == InstanceStatus.INIT ||
                                    taskInstance.getStatus() == InstanceStatus.WAITING ||
                                    taskInstance.getStatus() == InstanceStatus.RUNNING
                    );
        } else {
            isComplete = this.workflowInstanceRepository.findByTriggerId(logFileName)
                    .stream()
                    .noneMatch(workflowInstance -> workflowInstance.getStatus() == ProcessStatus.RUNNING || workflowInstance.getStatus() == ProcessStatus.SUSPENDED);
        }
        var fullName = logFileName + LogfilePostfix;
        String filePath = (isTask ? this.rootLocation : this.workflowLocation) + File.separator + fullName;
        var sseEmitter = this.template.newSseEmitter();
        if (isComplete) {
            this.firstReadLog(Paths.get(filePath), new AtomicLong(1), sseEmitter, size);
            return sseEmitter;
        }
        // 订阅未完成日志
        var consumerVo = this.monitoringFileService.listen(fullName, ((file, counter) -> {
            try {
                Files.lines(file).skip(counter.intValue())
                        .forEach(line -> {
                            this.template.sendMessage(SseEmitter.event()
                                    .id(String.valueOf(counter.get()))
                                    .data(line), sseEmitter);
                            counter.incrementAndGet();
                        });
            } catch (IOException e) {
                logger.trace("Could not read log file", e);
            }
        }));
        this.firstReadLog(Paths.get(filePath), consumerVo.getCounter(), sseEmitter, size);
        return sseEmitter;
    }

    private void firstReadLog(Path path, AtomicLong counter, SseEmitter sseEmitter, int size) {
        try (var linesCount = Files.lines(path);
             var lines = Files.lines(path)) {
            var countLine = linesCount.count();
            var startLine = countLine > size ? countLine - size : 0;
            counter.set(startLine);
            lines.skip(startLine)
                    .forEach(line -> this.template.sendMessage(SseEmitter.event()
                            .id(String.valueOf(counter.incrementAndGet()))
                            .data(line), sseEmitter)
                    );
            counter.set(countLine);
        } catch (IOException e) {
            logger.trace("Could not read log file", e);
        }
    }

    @Override
    public List<LogVo> randomReadLog(String logFileName, Integer line, Integer size, boolean isTask) {
        var filePath = (isTask ? this.rootLocation : this.workflowLocation) + File.separator + logFileName + LogfilePostfix;
        var list = new ArrayList<LogVo>();
        var lineNum = new AtomicLong(line - 1);
        try (var lines = Files.lines(Paths.get(filePath))) {
            lines.skip(line - 1)
                    .limit(size)
                    .forEach(str -> list.add(LogVo.builder()
                            .lastEventId(String.valueOf(lineNum.incrementAndGet()))
                            .data(str)
                            .build())
                    );
        } catch (IOException e) {
            logger.trace("Could not read log file", e);
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
        var filename = this.webhookRootLocation + File.separator + webhookFileName + webhookFilePostfix;
        try (var bufferedReader = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {
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

    @Override
    public void deleteWorkflowLog(String triggerId) {
        var file = new File(this.workflowLocation + File.separator + triggerId + LogfilePostfix);
        if (!file.delete()) {
            logger.warn("流程日志删除失败，triggerId：{}", triggerId);
        }
    }

    @Override
    public void deleteTaskLog(String taskId) {
        var file = new File(this.rootLocation + File.separator + taskId + LogfilePostfix);
        if (!file.delete()) {
            logger.warn("任务日志删除失败，taskInstanceId：{}", taskId);
        }
    }

    @Override
    public void deleteWebhook(String webhookRequestId) {
        var file = new File(this.webhookRootLocation + File.separator + webhookRequestId + webhookFilePostfix);
        if (!file.delete()) {
            logger.warn("webhook删除失败，webhookRequestId：{}", webhookRequestId);
        }
    }
}
