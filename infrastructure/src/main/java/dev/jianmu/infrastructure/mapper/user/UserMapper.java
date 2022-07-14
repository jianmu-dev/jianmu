package dev.jianmu.infrastructure.mapper.user;

import dev.jianmu.user.aggregate.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

/**
 * @author huangxi
 * @class UserMapper
 * @description UserMapper
 * @create 2022-6-30 10:59
 */
public interface UserMapper {
    @Select("SELECT * FROM `jm_user` WHERE id = #{id}")
    @Result(column = "id", property = "id")
    @Result(column = "avatar_url", property = "avatarUrl")
    @Result(column = "nickname", property = "nickname")
    @Result(column = "data", property = "data")
    @Result(column = "username", property = "username")
    Optional<User> findById(String id);

    @Insert("insert into jm_user(id, avatar_url, nickname, data, username) " +
            "values(#{id}, #{avatarUrl}, #{nickname}, #{data}, #{username})")
    void add(User user);

    @Update("update jm_user set avatar_url = #{avatarUrl}, nickname = #{nickname}, data = #{data}, username = #{username} where id = #{id}")
    void update(User user);
}
