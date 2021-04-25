package dev.jianmu.infrastructure.mapper.dsl;

import dev.jianmu.dsl.aggregate.DslSourceCode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @class: DslSourceCodeMapper
 * @description: DslSourceCodeMapper
 * @author: Ethan Liu
 * @create: 2021-04-25 14:28
 **/
public interface DslSourceCodeMapper {
    @Insert("")
    void add(DslSourceCode dslSourceCode);

    @Select("")
    Optional<DslSourceCode> findByRefAndVersion(String ref, String version);
}
