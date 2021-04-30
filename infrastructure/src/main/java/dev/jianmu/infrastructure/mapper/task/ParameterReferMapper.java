package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.ParameterRefer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class: ParameterReferMapper
 * @description: ParameterReferMapper
 * @author: Ethan Liu
 * @create: 2021-04-30 12:38
 **/
public interface ParameterReferMapper {
    @Insert("")
    void addAll(List<ParameterRefer> parameterRefers);

    @Select("")
    List<ParameterRefer> findByRefAndVersionAndTargetTaskRef(String workflowRef, String workflowVersion, String targetTaskRef);
}
