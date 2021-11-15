package dev.jianmu.trigger.repository;

import dev.jianmu.trigger.aggregate.WebRequest;

/**
 * @class: WebRequestRepository
 * @description: WebRequestRepository
 * @author: Ethan Liu
 * @create: 2021-11-14 22:19
 */
public interface WebRequestRepository {
    void add(WebRequest webRequest);
}
