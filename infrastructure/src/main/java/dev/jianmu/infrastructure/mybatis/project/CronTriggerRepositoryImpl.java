package dev.jianmu.infrastructure.mybatis.project;

import dev.jianmu.infrastructure.mapper.project.CronTriggerMapper;
import dev.jianmu.project.aggregate.CronTrigger;
import dev.jianmu.project.repository.CronTriggerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class: CronTriggerRepositoryImpl
 * @description: CronTriggerRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-05-25 12:33
 **/
@Repository
public class CronTriggerRepositoryImpl implements CronTriggerRepository {
    private final CronTriggerMapper cronTriggerMapper;

    public CronTriggerRepositoryImpl(CronTriggerMapper cronTriggerMapper) {
        this.cronTriggerMapper = cronTriggerMapper;
    }

    @Override
    public void add(CronTrigger cronTrigger) {
        this.cronTriggerMapper.add(cronTrigger);
    }

    @Override
    public void deleteByProjectId(String projectId) {
        this.cronTriggerMapper.deleteByProjectId(projectId);
    }

    @Override
    public void deleteById(String id) {
        this.cronTriggerMapper.deleteById(id);
    }

    @Override
    public Optional<CronTrigger> findById(String id) {
        return this.cronTriggerMapper.findById(id);
    }

    @Override
    public List<CronTrigger> findByProjectId(String projectId) {
        return this.cronTriggerMapper.findByProjectId(projectId);
    }
}
