package dev.jianmu.infrastructure.mapper.secret;

import dev.jianmu.secret.aggregate.Namespace;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * @class: NamespaceMapper
 * @description: 命名空间Mapper
 * @author: Ethan Liu
 * @create: 2021-04-20 13:32
 **/
public interface NamespaceMapper {
    @Insert("insert into secret_namespace(name, description) " +
            "values(#{name}, #{description})")
    void add(Namespace namespace);

    @Delete("delete from secret_namespace where name = #{name}")
    void delete(String name);

    @Select("select * from secret_namespace where name = #{name}")
    Optional<Namespace> findByName(String name);

    @Select("<script>" +
            "SELECT * FROM `secret_namespace` " +
            "<if test='name != null'> WHERE `name` like concat('%', #{name}, '%')</if>" +
            "</script>")
    List<Namespace> findAll(String name);
}
