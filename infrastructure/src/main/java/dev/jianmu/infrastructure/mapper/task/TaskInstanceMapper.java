package dev.jianmu.infrastructure.mapper.task;

import dev.jianmu.task.aggregate.TaskInstance;
import dev.jianmu.task.aggregate.InstanceStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * @class: InstanceMapper
 * @description: 任务实例Mapper
 * @author: Ethan Liu
 * @create: 2021-03-25 21:39
 **/
public interface TaskInstanceMapper {
    @Insert("insert into task_instance(id,key_version, def_key, def_version, name, description, business_id, status) " +
            "values(#{id},'${defKey + defVersion}', #{defKey}, #{defVersion}, #{name}, #{description}, #{businessId}, #{status})")
    void add(TaskInstance taskInstance);

    @Update("update task_instance set status = #{status} where id = #{instanceId}")
    void updateStatus(@Param("instanceId") String instanceId, @Param("status") InstanceStatus status);

    @Select("select * from task_instance where id = #{instanceId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "def_version", property = "defVersion")
    @Result(column = "business_id", property = "businessId")
    Optional<TaskInstance> findById(String instanceId);

    @Select("select * from task_instance where business_id = #{businessId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "def_version", property = "defVersion")
    @Result(column = "business_id", property = "businessId")
    List<TaskInstance> findByBusinessId(String businessId);

    @Select("select * from task_instance where key_version = #{keyVersion}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "def_version", property = "defVersion")
    @Result(column = "business_id", property = "businessId")
    List<TaskInstance> findByKeyVersion(String keyVersion);

    @Select("select * from task_instance where key_version = #{keyVersion} and business_id = #{businessId}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "def_version", property = "defVersion")
    @Result(column = "business_id", property = "businessId")
    List<TaskInstance> findByKeyVersionAndBusinessId(@Param("keyVersion") String keyVersion, @Param("businessId") String businessId);

    @Select("select * from task_instance where status = #{status}")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "def_version", property = "defVersion")
    @Result(column = "business_id", property = "businessId")
    List<TaskInstance> findByStatus(InstanceStatus status);

    @Select("select * from task_instance")
    @Result(column = "def_key", property = "defKey")
    @Result(column = "def_version", property = "defVersion")
    @Result(column = "business_id", property = "businessId")
    List<TaskInstance> findAll(
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );
}
