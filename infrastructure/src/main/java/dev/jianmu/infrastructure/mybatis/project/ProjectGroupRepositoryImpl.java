package dev.jianmu.infrastructure.mybatis.project;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.project.ProjectGroupMapper;
import dev.jianmu.project.aggregate.ProjectGroup;
import dev.jianmu.project.repository.ProjectGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Daihw
 * @class ProjectGroupRepositoryImpl
 * @description 项目组仓储实现
 * @create 2021/11/25 2:17 下午
 */
@Repository
public class ProjectGroupRepositoryImpl implements ProjectGroupRepository {
    private final ProjectGroupMapper projectGroupMapper;

    public ProjectGroupRepositoryImpl(ProjectGroupMapper projectGroupMapper) {
        this.projectGroupMapper = projectGroupMapper;
    }

    @Override
    public void add(ProjectGroup projectGroup) {
        this.projectGroupMapper.add(projectGroup);
    }

    @Override
    public void addAll(List<ProjectGroup> projectGroups) {
        this.projectGroupMapper.addAll(projectGroups);
    }

    @Override
    public void deleteById(String id) {
        this.projectGroupMapper.deleteById(id);
    }

    @Override
    public Optional<ProjectGroup> findById(String id) {
        return this.projectGroupMapper.findById(id);
    }

    @Override
    public Optional<ProjectGroup> findByName(String name) {
        return this.projectGroupMapper.findByName(name);
    }

    @Override
    public void update(ProjectGroup projectGroup) {
        this.projectGroupMapper.update(projectGroup);
    }

    @Override
    public List<ProjectGroup> findAllBySortBetween(Integer originSort, Integer targetSort) {
        return this.projectGroupMapper.findAllBySortBetween(originSort, targetSort);
    }

    @Override
    public Optional<ProjectGroup> findBySortMax() {
        return this.projectGroupMapper.findBySortMax();
    }

    @Override
    public void addProjectCountById(String projectGroupId, int count) {
        this.projectGroupMapper.addProjectCountById(projectGroupId, count);
    }

    @Override
    public void subProjectCountById(String projectGroupId, int count) {
        this.projectGroupMapper.subProjectCountById(projectGroupId, count);
    }

    @Override
    public void deleteByIdIn(List<String> ids) {
        if (ids.isEmpty()) {
            return;
        }
        this.projectGroupMapper.deleteByIdIn(ids);
    }

    public PageInfo<ProjectGroup> findPage(int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.projectGroupMapper.findAllOrderBySort());
    }
}
