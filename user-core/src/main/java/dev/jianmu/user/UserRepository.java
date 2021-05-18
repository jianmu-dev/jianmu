package dev.jianmu.user;

import java.util.Optional;

/**
 * @class: UserRepository
 * @description: UserRepository
 * @author: Ethan Liu
 * @create: 2021-05-17 21:59
 **/
public interface UserRepository {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
