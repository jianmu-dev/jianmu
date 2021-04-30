package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.InputParameter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @class: InputParameterMapper
 * @description: InputParameterMapper
 * @author: Ethan Liu
 * @create: 2021-04-30 12:39
 **/
public interface InputParameterMapper {
    @Insert("")
    void addAll(List<InputParameter> inputParameters);

    @Select("")
    List<InputParameter> findByWorkflowRefAndWorkflowVersionAndAsyncTaskRef(String workflowRef, String workflowVersion, String asyncTaskRef);
}
