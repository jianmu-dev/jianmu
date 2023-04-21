package dev.jianmu.infrastructure.mybatis.trigger;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.trigger.WebRequestMapper;
import dev.jianmu.trigger.aggregate.WebRequest;
import dev.jianmu.trigger.repository.WebRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Ethan Liu
 * @class WebRequestRepositoryImpl
 * @description WebRequestRepositoryImpl
 * @create 2021-11-15 13:27
 */
@Repository
public class WebRequestRepositoryImpl implements WebRequestRepository {
    private final WebRequestMapper webRequestMapper;

    public WebRequestRepositoryImpl(WebRequestMapper webRequestMapper) {
        this.webRequestMapper = webRequestMapper;
    }

    @Override
    public void add(WebRequest webRequest) {
        this.webRequestMapper.add(webRequest);
    }

    @Override
    public Optional<WebRequest> findById(String id) {
        return this.webRequestMapper.findById(id);
    }

    @Override
    public void update(WebRequest webRequest) {
        this.webRequestMapper.update(webRequest);
    }

    @Override
    public void deleteByProjectId(String projectId) {
        this.webRequestMapper.deleteByProjectId(projectId);
    }

    @Override
    public Optional<WebRequest> findByTriggerId(String triggerId) {
        return this.webRequestMapper.findByTriggerId(triggerId);
    }

    @Override
    public Optional<WebRequest> findLatestByProjectId(String projectId) {
        return this.webRequestMapper.findLatestByProjectId(projectId);
    }

    public PageInfo<WebRequest> findPage(String projectId, int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.webRequestMapper.findPage(projectId));
    }
}
