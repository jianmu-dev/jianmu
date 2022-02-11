package dev.jianmu.infrastructure.mybatis.user;

import dev.jianmu.infrastructure.GlobalProperties;
import dev.jianmu.user.User;
import dev.jianmu.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Ethan Liu
 * @class UserRepositoryImpl
 * @description UserRepositoryImpl
 * @create 2021-05-18 08:49
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final GlobalProperties globalProperties;

    private final PasswordEncoder passwordEncoder;

    public UserRepositoryImpl(GlobalProperties globalProperties) {
        this.globalProperties = globalProperties;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (!username.equals(this.globalProperties.getApi().getAdminUser())) {
            return Optional.empty();
        }
        var user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(this.globalProperties.getApi().getAdminPasswd()));
        return Optional.of(user);
    }
}
