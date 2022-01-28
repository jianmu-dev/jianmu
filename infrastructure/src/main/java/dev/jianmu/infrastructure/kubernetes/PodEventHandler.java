package dev.jianmu.infrastructure.kubernetes;

import io.kubernetes.client.openapi.models.V1Pod;

/**
 * @author Ethan Liu
 * @class PodEventHandler
 * @description PodEventHandler
 * @create 2022-01-23 22:15
 */
public interface PodEventHandler {
    void handleEvt(V1Pod v1Pod);
}
