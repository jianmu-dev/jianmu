package dev.jianmu.infrastructure.mapper.secret;

import dev.jianmu.secret.aggregate.Namespace;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: NamespaceMapper
 * @description: 命名空间Mapper
 * @author: Ethan Liu
 * @create: 2021-04-20 13:32
 **/
public interface NamespaceMapper {
    @Insert("insert into secret_namespace(name, description, created_time, last_modified_time) " +
            "values(#{name}, #{description}, #{createdTime}, #{lastModifiedTime})")
    void add(Namespace namespace);

    @Delete("delete from secret_namespace where name = #{name}")
    void delete(String name);

    @Select("select * from secret_namespace where name = #{name}")
    Optional<Namespace> findByName(String name);

    @Update("update secret_namespace set last_modified_time = #{lastModifiedTime} where name = #{name}")
    void updateLastModifiedTime(Namespace namespace);

    @Select("SELECT * FROM `secret_namespace`")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<Namespace> findAll();
}
