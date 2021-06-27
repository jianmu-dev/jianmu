package dev.jianmu.infrastructure.mybatis.user;

import dev.jianmu.infrastructure.jwt.JwtProperties;
import dev.jianmu.user.User;
import dev.jianmu.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @class: UserRepositoryImpl
 * @description: UserRepositoryImpl
 * @author: Ethan Liu
 * @create: 2021-05-18 08:49
 **/
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JwtProperties jwtProperties;

    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (!username.equals(jwtProperties.getAdminUser())) {
            return Optional.empty();
        }
        var user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(jwtProperties.getAdminPasswd()));
        return Optional.of(user);
    }
}
