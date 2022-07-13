package dev.jianmu.user.repository;

import dev.jianmu.user.aggregate.User;

import java.util.Optional;

/**
 * @author Ethan Liu
 * @class UserRepository
 * @description UserRepository
 * @create 2021-05-17 21:59
 */

public interface UserRepository {
    void add(User user);

    void update(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findById(String id);
}
