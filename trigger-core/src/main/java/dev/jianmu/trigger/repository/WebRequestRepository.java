package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.WebRequest;

import java.util.Optional;

/**
 * @author Ethan Liu
 * @class WebRequestRepository
 * @description WebRequestRepository
 * @create 2021-11-14 22:19
 */
public interface WebRequestRepository {
    void add(WebRequest webRequest);

    Optional<WebRequest> findById(String id);

    void update(WebRequest webRequest);

    void deleteByProjectId(String projectId);

    Optional<WebRequest> findByTriggerId(String triggerId);

    Optional<WebRequest> findLatestByProjectId(String projectId);
}
