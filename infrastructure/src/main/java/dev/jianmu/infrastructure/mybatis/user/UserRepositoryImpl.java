package dev.jianmu.infrastructure.mybatis.user;

import dev.jianmu.user.User;
import dev.jianmu.user.UserRepository;
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
    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Boolean existsByUsername(String username) {
        return null;
    }

    @Override
    public Boolean existsByEmail(String email) {
        return null;
    }
}
