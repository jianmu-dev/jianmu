package dev.jianmu.infrastructure.mybatis.trigger;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import dev.jianmu.infrastructure.mapper.trigger.WebRequestMapper;
import dev.jianmu.trigger.aggregate.WebRequest;
import dev.jianmu.trigger.repository.WebRequestRepository;
import org.springframework.stereotype.Repository;

/**
 * @class WebRequestRepositoryImpl
 * @description WebRequestRepositoryImpl
 * @author Ethan Liu
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

    public PageInfo<WebRequest> findPage(String projectId, int pageNum, int pageSize) {
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> this.webRequestMapper.findPage(projectId));
    }
}
