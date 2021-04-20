package dev.jianmu.infrastructure.mapper.secret;

import dev.jianmu.secret.aggregate.KVPair;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: KVPairMapper
 * @description: 键值对Mapper
 * @author: Ethan Liu
 * @create: 2021-04-20 13:31
 **/
public interface KVPairMapper {
    @Insert("insert into secret_kv_pair(namespace_name, kv_key, kv_value) " +
            "values(#{namespaceName}, #{key}, #{value})")
    void add(KVPair kvPair);

    @Delete("delete from secret_kv_pair where namespace_name = #{namespaceName} and kv_key = #{key}")
    void deleteByNameAndKey(@Param("namespaceName") String namespaceName, @Param("key") String key);

    @Delete("delete from secret_kv_pair where namespace_name = #{namespaceName}")
    void deleteByName(@Param("namespaceName") String namespaceName);

    @Select("select * from secret_kv_pair where kv_key = #{key}")
    @Result(column = "namespace_name", property = "namespaceName")
    @Result(column = "kv_key", property = "key")
    @Result(column = "kv_value", property = "value")
    Optional<KVPair> findByKey(@Param("key") String key);

    @Select("select * from secret_kv_pair namespace_name = #{namespaceName}")
    @Result(column = "namespace_name", property = "namespaceName")
    @Result(column = "kv_key", property = "key")
    @Result(column = "kv_value", property = "value")
    List<KVPair> findByNamespaceName(@Param("namespaceName") String namespaceName);
}
