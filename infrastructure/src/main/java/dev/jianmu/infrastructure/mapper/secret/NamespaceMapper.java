package dev.jianmu.infrastructure.mapper.secret;

import dev.jianmu.secret.aggregate.Namespace;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class NamespaceMapper
 * @description 命名空间Mapper
 * @create 2021-04-20 13:32
 */
public interface NamespaceMapper {
    @Insert("insert into jm_secret_namespace(association_id, association_type, association_platform, name, description, created_time, last_modified_time) " +
            "values(#{associationId}, #{associationType}, #{associationPlatform}, #{name}, #{description}, #{createdTime}, #{lastModifiedTime})")
    void add(Namespace namespace);

    @Delete("<script>" +
            "DELETE FROM jm_secret_namespace " +
            "<where> name = #{name}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            " <if test='associationPlatform != null'> AND association_platform = #{associationPlatform} </if>" +
            "</where>" +
            "</script>")
    void delete(@Param("associationId") String associationId,
                @Param("associationType") String associationType,
                @Param("associationPlatform") String associationPlatform,
                @Param("name") String name);

    @Select("<script>" +
            "select * from jm_secret_namespace where name = #{name}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            " <if test='associationPlatform != null'> AND association_platform = #{associationPlatform} </if>" +
            "</script>")
    Optional<Namespace> findByName(@Param("associationId") String associationId,
                                   @Param("associationType") String associationType,
                                   @Param("associationPlatform") String associationPlatform,
                                   @Param("name") String name);

    @Update("<script>" +
            "update jm_secret_namespace set last_modified_time = #{lastModifiedTime} " +
            "<where>" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            " <if test='associationPlatform != null'> AND association_platform = #{associationPlatform} </if>" +
            "</where>" +
            "</script>")
    void updateLastModifiedTime(Namespace namespace);

    @Select("<script>" +
            "SELECT * FROM `jm_secret_namespace`" +
            "<where>" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            " <if test='associationPlatform != null'> AND association_platform = #{associationPlatform} </if>" +
            "</where>" +
            "ORDER BY `created_time` desc" +
            "</script>")
    @Result(column = "created_time", property = "createdTime")
    @Result(column = "last_modified_time", property = "lastModifiedTime")
    List<Namespace> findAll(@Param("associationId") String associationId,
                            @Param("associationType") String associationType,
                            @Param("associationPlatform") String associationPlatform);

    @Delete("DELETE FROM `jm_secret_namespace` WHERE `association_id` = #{associationId} AND `association_type` = #{associationType} AND `association_platform` = #{associationPlatform}")
    void deleteByAssociationIdAndType(@Param("associationId") String associationId,
                                      @Param("associationType") String associationType,
                                      @Param("associationPlatform") String associationPlatform);
}
