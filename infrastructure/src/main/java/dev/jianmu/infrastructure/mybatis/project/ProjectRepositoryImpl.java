package dev.jianmu.infrastructure.mybatis.project;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.project.ProjectMapper;
import dev.jianmu.project.aggregate.Project;
import dev.jianmu.project.query.ProjectVo;
import dev.jianmu.project.repository.ProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @class DslReferenceRepositoryImpl
 * @description DSL流程定义关联仓储实现
 * @author Ethan Liu
 * @create 2021-04-23 11:39
*/
@Repository
public class ProjectRepositoryImpl implements ProjectRepository {
    private final ProjectMapper projectMapper;

    public ProjectRepositoryImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public void add(Project project) {
        if (project.getAssociationId() == null || project.getAssociationType() == null) {
            project.setAssociationId("");
            project.setAssociationType("");
        }
        this.projectMapper.add(project);
    }

    @Override
    public void deleteByWorkflowRef(String workflowRef) {
        this.projectMapper.deleteByWorkflowRef(workflowRef);
    }

    @Override
    public void updateByWorkflowRef(Project project) {
        this.projectMapper.updateByWorkflowRef(project);
    }

    @Override
    public Optional<Project> findById(String id) {
        return this.projectMapper.findById(id);
    }

    @Override
    public Optional<Project> findByName(String associationId, String associationType, String name) {
        return this.projectMapper.findByName(associationId, associationType, name);
    }

    @Override
    public Optional<Project> findByWorkflowRef(String workflowRef) {
        return this.projectMapper.findByWorkflowRef(workflowRef);
    }

    @Override
    public List<ProjectVo> findByIdIn(List<String> ids) {
        return this.projectMapper.findByIdIn(ids);
    }

    public PageInfo<Project> findAllPage(String workflowName, int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.projectMapper.findAllPage(workflowName));
    }

    public List<Project> findAll() {
        return this.projectMapper.findAll();
    }

    public PageInfo<ProjectVo> findPageByGroupId(Integer pageNum, Integer pageSize, String projectGroupId, String workflowName, String sortType, String associationId, String associationType) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.projectMapper.findAllByGroupId(projectGroupId, workflowName, sortType, associationId, associationType));
    }
}
