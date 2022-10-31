package dev.jianmu.git.repo.repository;

import dev.jianmu.git.repo.aggregate.User;

/**
 * @author Daihw
 * @class UserRepository
 * @description UserRepository
 * @create 2022/10/27 10:28 上午
 */
public interface UserRepository {
    User getByUsername(String username);
}
