package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.infrastructure.storage.MonitoringFileService;
import dev.jianmu.task.aggregate.NodeInfo;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.Volume;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.repository.VolumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-24 08:08
 */
@Service
@Slf4j
public class CacheApplication {
    private final VolumeRepository volumeRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final MonitoringFileService monitoringFileService;

    public CacheApplication(
            VolumeRepository volumeRepository,
            TaskInstanceRepository taskInstanceRepository,
            MonitoringFileService monitoringFileService
    ) {
        this.volumeRepository = volumeRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.monitoringFileService = monitoringFileService;
    }

    @Transactional
    public void create(String projectId, String name) {
        var volume = Volume.Builder.aVolume()
                .name(name)
                .scope(Volume.Scope.PROJECT)
                .projectId(projectId)
                .build();
        var task = TaskInstance.Builder.anInstance()
                .serialNo(1)
                .defKey("start")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("start").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .build();
        this.volumeRepository.create(volume);
        this.taskInstanceRepository.add(task);
    }

    @Transactional
    public void activate(String id, String workerId) {
        var volume = this.volumeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到待激活的缓存"));
        volume.activate(workerId);
        this.volumeRepository.activate(volume);
    }

    @Transactional
    public void taint(String id) {
        var volume = this.volumeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到要标记的缓存"));
        volume.taint();
        this.volumeRepository.taint(volume);
    }

    @Transactional
    public void deleteById(String id) {
        var volume = this.volumeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到要删除的Cache"));
        var task = TaskInstance.Builder.anInstance()
                .serialNo(1)
                .defKey("end")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("end").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .build();
        this.taskInstanceRepository.add(task);
    }

    public void executeSucceeded(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        MDC.put("triggerId", taskInstance.getTriggerId());
        if (taskInstance.isDeletionVolume()) {
            // 成功删除Volume
            this.volumeRepository.deleteByName(taskInstance.getTriggerId());
            log.info("删除Volume: {}", taskInstance.getTriggerId());
            this.monitoringFileService.clearCallbackByLogId(taskInstance.getTriggerId());
        }
        // 查找Volume并激活
        // TODO 为兼容老数据有可能存在未存储的Volume记录
        this.volumeRepository.findByName(taskInstance.getTriggerId())
                .ifPresent(volume -> {
                    volume.activate(taskInstance.getWorkerId());
                    this.volumeRepository.activate(volume);
                });
    }

    public void executeFailed(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        MDC.put("triggerId", taskInstance.getTriggerId());
        if (taskInstance.isCreationVolume()) {
            log.error("创建Volume失败");
            // 创建Volume失败，删除Volume记录
            this.volumeRepository.deleteByName(taskInstance.getTriggerId());
            log.info("删除Volume: {}", taskInstance.getTriggerId());
        } else {
            log.error("清除Volume失败");
            // 清除Volume失败，标记Volume记录
            this.volumeRepository.findByName(taskInstance.getTriggerId())
                    .ifPresent(volume -> {
                        volume.taint();
                        this.volumeRepository.taint(volume);
                    });
            this.monitoringFileService.clearCallbackByLogId(taskInstance.getTriggerId());
        }
    }
}
