package dev.jianmu.infrastructure.mybatis.task;

import dev.jianmu.infrastructure.mapper.task.VolumeMapper;
import dev.jianmu.task.aggregate.Volume;
import dev.jianmu.task.repository.VolumeRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author <a href="https://gitee.com/ethan-liu">Ethan Liu</a>
 * @date 2023-02-19 11:07
 */
@Repository
public class VolumeRepositoryImpl implements VolumeRepository {
    private final VolumeMapper volumeMapper;

    public VolumeRepositoryImpl(VolumeMapper volumeMapper) {
        this.volumeMapper = volumeMapper;
    }

    @Override
    public void create(Volume volume) {
        this.volumeMapper.create(volume);
    }

    @Override
    public Optional<Volume> findById(String id) {
        return this.volumeMapper.findById(id);
    }

    @Override
    public Optional<Volume> findByName(String name) {
        return this.volumeMapper.findByName(name);
    }

    @Override
    public void activate(Volume volume) {
        this.volumeMapper.activate(volume);
    }

    @Override
    public void taint(Volume volume) {
        this.volumeMapper.taint(volume);
    }

    @Override
    public void deleteById(String id) {
        this.volumeMapper.deleteById(id);
    }

    @Override
    public void deleteByName(String name) {
        this.volumeMapper.deleteByName(name);
    }
}
