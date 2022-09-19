package dev.jianmu.infrastructure.sse;

import dev.jianmu.event.Event;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WorkflowInstanceStatusSubscribeService {
    private final Map<String, Map<String, CopyOnWriteArrayList<SseEmitter>>> workflowMap = new ConcurrentHashMap<>();

    public SseEmitter newSseEmitter(String workflowRef, String workflowInstanceId) {
        var sseEmitter = new SseEmitter(0L);
        sseEmitter.onError(e -> this.clearByWorkflowInstanceId(workflowRef, workflowInstanceId));
        this.workflowMap.putIfAbsent(workflowRef, new ConcurrentHashMap<>());
        var workflowInstanceMap = this.workflowMap.get(workflowRef);
        workflowInstanceMap.putIfAbsent(workflowInstanceId, new CopyOnWriteArrayList<>());
        workflowInstanceMap.get(workflowInstanceId).add(sseEmitter);
        return sseEmitter;
    }

    public void sendWorkflowInstanceMessage(String workflowRef, String workflowInstanceId, Event event) {
        var workflowInstanceMap = this.workflowMap.get(workflowRef);
        if (workflowInstanceMap == null) {
            return;
        }
        var sseEmitters = workflowInstanceMap.get(workflowInstanceId);
        if (sseEmitters == null) {
            return;
        }
        sseEmitters.forEach(sseEmitter -> {
            try {
                sseEmitter.send(SseEmitter.event()
                        .id(workflowInstanceId)
                        .data(event));
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        });
    }

    public void sendWorkflowInstanceMessage(String workflowRef, Event event) {
        var workflowInstanceMap = this.workflowMap.get(workflowRef);
        if (workflowInstanceMap == null) {
            return;
        }
        workflowInstanceMap.forEach((key, value) -> value.forEach(sseEmitter -> {
            try {
                sseEmitter.send(SseEmitter.event()
                        .id(workflowRef)
                        .data(event));
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }));
    }

    public void clearByWorkflowInstanceId(String workflowRef, String workflowInstanceId) {
        var workflowInstanceMap = this.workflowMap.get(workflowRef);
        if (workflowInstanceMap == null) {
            return;
        }
        workflowInstanceMap.remove(workflowInstanceId);
        if (workflowInstanceMap.isEmpty()) {
            this.workflowMap.remove(workflowRef);
        }
    }
}
