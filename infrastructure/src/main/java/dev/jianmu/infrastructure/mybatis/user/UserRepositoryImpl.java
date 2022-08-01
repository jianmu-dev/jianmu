package dev.jianmu.infrastructure.mybatis.user;

import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.infrastructure.mapper.user.UserMapper;
import dev.jianmu.user.aggregate.User;
import dev.jianmu.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author huangxi
 * @class UserRepositoryImpl
 * @description UserRepositoryImpl
 * @create 2022-06-30 13:51
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(JwtProperties jwtProperties, UserMapper userMapper) {
        this.jwtProperties = jwtProperties;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void add(User user) {
        this.userMapper.add(user);
    }

    @Override
    public void update(User user) {
        this.userMapper.update(user);
    }

    @Override
    public Optional<User> findByName(String username) {
        if (!username.equals(this.jwtProperties.getAdminUser())) {
            return Optional.empty();
        }
        return Optional.of(User.Builder.aReference()
                .username(username)
                .id("1")
                .build());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return this.userMapper.findByUsername(username);
    }

    @Override
    public Optional<User> findById(String id) {
        return this.userMapper.findById(id);
    }

}
