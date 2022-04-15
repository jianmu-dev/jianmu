package dev.jianmu.api.mapper;

import dev.jianmu.api.vo.AsyncTaskInstanceVo;
import dev.jianmu.workflow.aggregate.process.AsyncTaskInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Ethan Liu
 * @class AsyncTaskInstanceMapper
 * @description 异步任务实例Mapper
 * @create 2022-04-13 20:51
 */
@Mapper
public interface AsyncTaskInstanceMapper {
    AsyncTaskInstanceMapper INSTANCE = Mappers.getMapper(AsyncTaskInstanceMapper.class);

    AsyncTaskInstanceVo toAsyncTaskInstanceVo(AsyncTaskInstance asyncTaskInstance);
}
