package dev.jianmu.application.service;

import dev.jianmu.application.exception.DataNotFoundException;
import dev.jianmu.task.aggregate.NodeInfo;
import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.Volume;
import dev.jianmu.task.repository.TaskInstanceRepository;
import dev.jianmu.task.repository.VolumeRepository;
import lombok.extern.slf4j.Slf4j;
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

    public CacheApplication(VolumeRepository volumeRepository, TaskInstanceRepository taskInstanceRepository) {
        this.volumeRepository = volumeRepository;
        this.taskInstanceRepository = taskInstanceRepository;
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
}
