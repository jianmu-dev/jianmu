package dev.jianmu.infrastructure.mapper.user;

import dev.jianmu.user.aggregate.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * @author huangxi
 * @class UserMapper
 * @description UserMapper
 * @create 2022-6-30 10:59
 */
public interface UserMapper {
    @Select("SELECT * FROM `user` WHERE id = #{id}")
    @Result(column = "id", property = "id")
    @Result(column = "head_url", property = "headUrl")
    @Result(column = "nickname", property = "nickname")
    @Result(column = "data", property = "data")
    @Result(column = "username", property = "username")
    Optional<User> findById(String id);

    @Insert("insert into user(id, head_url, nickname, data, username) " +
            "values(#{id}, #{headUrl}, #{nickname}, #{data}, #{username})")
    void add(User user);
}