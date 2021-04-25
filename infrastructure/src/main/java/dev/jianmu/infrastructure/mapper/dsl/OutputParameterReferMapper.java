package dev.jianmu.infrastructure.mapper.dsl;

import dev.jianmu.dsl.aggregate.OutputParameterRefer;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @class: OutputParameterReferMapper
 * @description: OutputParameterReferMapper
 * @author: Ethan Liu
 * @create: 2021-04-25 14:28
 **/
public interface OutputParameterReferMapper {
    @Insert("")
    void addAll(Set<OutputParameterRefer> refers);

    @Select("")
    List<OutputParameterRefer> findByContextId(String contextId);
}
