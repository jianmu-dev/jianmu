package dev.jianmu.api.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jianmu.application.service.TaskInstanceApplication;
import dev.jianmu.application.service.WorkerApplication;
import dev.jianmu.infrastructure.messagequeue.TaskInstanceQueue;
import dev.jianmu.infrastructure.storage.StorageException;
import dev.jianmu.infrastructure.storage.StorageService;
import dev.jianmu.task.aggregate.TaskInstance;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @class: WorkerStreamServiceImpl
 * @description: Grpc接口实现类
 * @author: Ethan Liu
 * @create: 2021-03-30 14:24
 **/
@GrpcService
public class WorkerStreamServiceImpl extends WorkerStreamServiceGrpc.WorkerStreamServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(WorkerStreamServiceImpl.class);
    private final WorkerApplication workerApplication;
    private final TaskInstanceApplication taskInstanceApplication;
    private final TaskInstanceQueue taskInstanceQueue;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;

    private final ConcurrentHashMap<String, Thread> threadMap = new ConcurrentHashMap<>();

    @Inject
    public WorkerStreamServiceImpl(
            WorkerApplication workerApplication,
            TaskInstanceApplication taskInstanceApplication,
            TaskInstanceQueue taskInstanceQueue,
            ObjectMapper objectMapper,
            StorageService storageService
    ) {
        this.workerApplication = workerApplication;
        this.taskInstanceApplication = taskInstanceApplication;
        this.taskInstanceQueue = taskInstanceQueue;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    @Override
    public void registry(OnlineReq request, StreamObserver<ServerResp> responseObserver) {
        String workerId = request.getWorkerId();
        logger.info("get worker (id: {}) registry request", workerId);
        this.workerApplication.online(workerId);
        responseObserver.onNext(
                ServerResp.newBuilder()
                        .setResult(true)
                        .setErrorMsg("200 OK")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void leave(OffLineReq request, StreamObserver<ServerResp> responseObserver) {
        String workerId = request.getWorkerId();
        logger.info("get worker (id: {}) leave request", workerId);
        this.workerApplication.offline(workerId);
        responseObserver.onNext(
                ServerResp.newBuilder()
                        .setResult(true)
                        .setErrorMsg("200 OK")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void subscribeTask(SubReq request, StreamObserver<Task> responseObserver) {
        for (; ; ) {
            try {
                Thread thread = this.threadMap.get(request.getWorkerId());
                if (thread != null && !Thread.currentThread().equals(thread)) {
                    logger.info("old subscriber kill, worker id: {} thread name: {}", request.getWorkerId(), thread.getName());
                    thread.interrupt();
                }
                this.threadMap.put(request.getWorkerId(), Thread.currentThread());
                TaskInstance taskInstance = this.taskInstanceQueue.take();
                logger.info("get task instance here: id {}, key {}", taskInstance.getId(), taskInstance.getDefKey());
                Task task = Task.newBuilder()
                        .setId(taskInstance.getId())
                        .build();
                responseObserver.onNext(task);
            } catch (InterruptedException e) {
                responseObserver.onCompleted();
                break;
            }
        }
    }

    @Override
    public void updateTask(TaskResult request, StreamObserver<ServerResp> responseObserver) {
        String taskId = request.getTaskId();
        logger.info("get task (id: {}) update request", taskId);
        TaskResult.Status status = request.getTaskStatus();
        logger.info("update task status: {}", status);
        Instant instant = Instant.ofEpochSecond(request.getWorkerTimestamp().getSeconds(),
                request.getWorkerTimestamp().getNanos());
        logger.info("update task time is: {}", instant.atZone(ZoneId.systemDefault()).toLocalDateTime());
        // TODO 需要生成任务记录
        switch (status) {
            case RUNNING:
                this.taskInstanceApplication.running(taskId);
                break;
            case FAILED:
                this.taskInstanceApplication.executeFailed(taskId);
                break;
            case SUCCEEDED:
                this.taskInstanceApplication.executeSucceeded(taskId);
                break;
            default:
                logger.info("should never get here");
        }
        responseObserver.onNext(
                ServerResp.newBuilder()
                        .setResult(true)
                        .setErrorMsg("200 OK")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<TaskOutput> uploadTaskLog(StreamObserver<ServerResp> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(TaskOutput taskOutput) {
                logger.info("get task (id: {}) output", taskOutput.getTaskId());
                try (var writer = storageService.writeLog(taskOutput.getTaskId())) {
                    writer.write(taskOutput.getLine());
                    writer.flush();
                } catch (IOException e) {
                    throw new StorageException("Could not write log file", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("get throwable is: ", throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(
                        ServerResp.newBuilder()
                                .setResult(true)
                                .setErrorMsg("200 OK")
                                .build()
                );
                responseObserver.onCompleted();
            }
        };
    }
}
