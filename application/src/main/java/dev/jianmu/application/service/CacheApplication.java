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

import java.util.List;

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
    public void create(String workflowRef, String name) {
        var volume = Volume.Builder.aVolume()
                .name(name)
                .scope(Volume.Scope.PROJECT)
                .workflowRef(workflowRef)
                .build();
        var taskInstances = this.taskInstanceRepository.findByBusinessId(volume.getId());
        var task = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey("start")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("start").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .workflowRef(workflowRef)
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
    public void clean(String id) {
        var volume = this.volumeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到要清理的Cache"));
        if (volume.isCleaning()) {
            throw new RuntimeException("缓存正在清理……");
        }
        var taskInstances = this.taskInstanceRepository.findByBusinessId(volume.getId());
        volume.clean();
        var task = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey("end")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("end").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .workflowRef(volume.getWorkflowRef())
                .build();
        this.volumeRepository.clean(volume);
        this.taskInstanceRepository.add(task);
    }

    @Transactional
    public void deleteById(String id) {
        var volume = this.volumeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("未找到要删除的Cache"));
        var taskInstances = this.taskInstanceRepository.findByBusinessId(volume.getId());
        var task = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey("end")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("end").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .workflowRef(volume.getWorkflowRef())
                .build();
        this.taskInstanceRepository.add(task);
    }

    @Transactional
    public void deleteByNameAndWorkflowRef(String name, String workflowRef) {
        var volume = this.volumeRepository.findByNameAndWorkflowRef(name, workflowRef)
                .orElseThrow(() -> new DataNotFoundException("未找到要删除的Cache"));
        var taskInstances = this.taskInstanceRepository.findByBusinessId(volume.getId());
        var task = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey("end")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("end").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .workflowRef(workflowRef)
                .build();
        this.taskInstanceRepository.add(task);
    }

    @Transactional
    public void deleteByWorkflowRef(String workflowRef) {
        this.volumeRepository.findByWorkflowRef(workflowRef).forEach(volume -> {
            var taskInstances = this.taskInstanceRepository.findByBusinessId(volume.getId());
            var task = TaskInstance.Builder.anInstance()
                    .serialNo(taskInstances.size() + 1)
                    .defKey("end")
                    .nodeInfo(NodeInfo.Builder.aNodeDef().name("end").build())
                    .asyncTaskRef("cache")
                    .businessId(volume.getId())
                    .triggerId(volume.getMountName())
                    .workflowRef(workflowRef)
                    .build();
            this.taskInstanceRepository.add(task);
        });
    }

    @Transactional
    public void executeSucceeded(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        MDC.put("triggerId", taskInstance.getTriggerId());
        var volume = this.volumeRepository.findByNameAndWorkflowRef(this.findVolumeName(taskInstance.getTriggerId()), taskInstance.getWorkflowRef())
                .orElseThrow(() -> new DataNotFoundException("未找到volume：" + taskInstance.getTriggerId()));
        if (taskInstance.isCreationVolume()) {
            // 查找Volume并激活
            // TODO 为兼容老数据有可能存在未存储的Volume记录
            volume.activate(taskInstance.getWorkerId());
            this.volumeRepository.activate(volume);
            return;
        }

        // 重建Volume
        if (volume.isCleaning()) {
            this.reBuild(volume);
            return;
        }
        // 成功删除Volume
        this.volumeRepository.deleteByNameAndWorkflowRef(this.findVolumeName(taskInstance.getTriggerId()), taskInstance.getWorkflowRef());
        log.info("删除Volume: {}", taskInstance.getTriggerId());
        this.monitoringFileService.clearCallbackByLogId(taskInstance.getTriggerId());

    }

    private String findVolumeName(String triggerId) {
        var arr = triggerId.split("_", 2);
        return arr.length == 1 ? arr[0] : arr[1];
    }

    private void reBuild(Volume volume) {
        var taskInstances = this.taskInstanceRepository.findByBusinessId(volume.getId());
        var task = TaskInstance.Builder.anInstance()
                .serialNo(taskInstances.size() + 1)
                .defKey("start")
                .nodeInfo(NodeInfo.Builder.aNodeDef().name("start").build())
                .asyncTaskRef("cache")
                .businessId(volume.getId())
                .triggerId(volume.getMountName())
                .workflowRef(volume.getWorkflowRef())
                .build();
        this.taskInstanceRepository.add(task);
    }

    @Transactional
    public void executeFailed(String taskInstanceId) {
        TaskInstance taskInstance = this.taskInstanceRepository.findById(taskInstanceId)
                .orElseThrow(() -> new DataNotFoundException("未找到该任务实例"));
        MDC.put("triggerId", taskInstance.getTriggerId());
        if (taskInstance.isCreationVolume()) {
            log.error("创建Volume失败");
            // 创建Volume失败，删除Volume记录
            this.volumeRepository.deleteByNameAndWorkflowRef(this.findVolumeName(taskInstance.getTriggerId()), taskInstance.getWorkflowRef());
            log.info("删除Volume: {}", taskInstance.getTriggerId());
        } else {
            log.error("清除Volume失败");
            // 清除Volume失败，标记Volume记录
            this.volumeRepository.findByNameAndWorkflowRef(this.findVolumeName(taskInstance.getTriggerId()), taskInstance.getWorkflowRef())
                    .ifPresent(volume -> {
                        volume.taint();
                        this.volumeRepository.taint(volume);
                    });
            this.monitoringFileService.clearCallbackByLogId(taskInstance.getTriggerId());
        }
    }

    public List<Volume> findByWorkflowRefAndScope(String workflowRef, Volume.Scope scope) {
        return this.volumeRepository.findByWorkflowRefAndScope(workflowRef, scope);
    }
}
