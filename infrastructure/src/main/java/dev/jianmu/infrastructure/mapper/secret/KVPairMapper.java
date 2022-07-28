package dev.jianmu.infrastructure.mapper.secret;

import dev.jianmu.secret.aggregate.KVPair;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Ethan Liu
 * @class KVPairMapper
 * @description 键值对Mapper
 * @create 2021-04-20 13:31
 */
public interface KVPairMapper {
    @Insert("insert into jm_secret_kv_pair(association_id, association_type, namespace_name, kv_key, kv_value, created_time) " +
            "values(#{associationId}, #{associationType}, #{namespaceName}, #{key}, #{value}, #{createdTime})")
    void add(KVPair kvPair);

    @Delete("<script>" +
            "delete from jm_secret_kv_pair " +
            "<where> namespace_name = #{namespaceName} and kv_key = #{key}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    void deleteByNameAndKey(@Param("associationId") String associationId,
                            @Param("associationType") String associationType,
                            @Param("namespaceName") String namespaceName,
                            @Param("key") String key);

    @Delete("<script>" +
            "delete from jm_secret_kv_pair " +
            "<where> namespace_name = #{namespaceName}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    void deleteByName(@Param("associationId") String associationId,
                      @Param("associationType") String associationType,
                      @Param("namespaceName") String namespaceName);

    @Select("<script>" +
            "select * from jm_secret_kv_pair " +
            "<where> namespace_name = #{namespaceName} and kv_key = #{key}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "</script>")
    @Result(column = "namespace_name", property = "namespaceName")
    @Result(column = "kv_key", property = "key")
    @Result(column = "kv_value", property = "value")
    @Result(column = "created_time", property = "createdTime")
    Optional<KVPair> findByNamespaceNameAndKey(@Param("associationId") String associationId,
                                               @Param("associationType") String associationType,
                                               @Param("namespaceName") String namespaceName,
                                               @Param("key") String key);

    @Select("<script>" +
            "select * from jm_secret_kv_pair " +
            "<where> namespace_name = #{namespaceName}" +
            " <if test='associationId != null'> AND association_id = #{associationId} </if>" +
            " <if test='associationType != null'> AND association_type = #{associationType} </if>" +
            "</where>" +
            "order by created_time desc" +
            "</script>")
    @Result(column = "namespace_name", property = "namespaceName")
    @Result(column = "kv_key", property = "key")
    @Result(column = "kv_value", property = "value")
    @Result(column = "created_time", property = "createdTime")
    List<KVPair> findByNamespaceName(@Param("associationId") String associationId,
                                     @Param("associationType") String associationType,
                                     @Param("namespaceName") String namespaceName);
}
