package dev.jianmu.infrastructure;

import dev.jianmu.infrastructure.storage.MonitoringFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ethan Liu
 * @class SseTemplate
 * @description SseTemplate
 * @create 2022-05-11 08:28
 */
@Service
public class SseTemplate {
    private final MonitoringFileService monitoringFileService;
    private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();

    public SseTemplate(MonitoringFileService monitoringFileService) {
        this.monitoringFileService = monitoringFileService;
    }

    public SseEmitter newSseEmitter(String topic, Long sseTimeout) {
        SseEmitter emitter = new SseEmitter(sseTimeout);
        connections.put(topic, emitter);
        emitter.onTimeout(() -> this.removeEmitter(topic));
        emitter.onError(throwable -> this.removeEmitter(topic));
        emitter.onCompletion(() -> this.removeEmitter(topic));
        return emitter;
    }

    public void broadcast(String topic, SseEmitter.SseEventBuilder event) {
        Optional.ofNullable(connections.get(topic))
                .ifPresent(sseEmitter ->
                        this.sendMessage(event, sseEmitter));
    }

    public void sendMessage(SseEmitter.SseEventBuilder event, SseEmitter sseEmitter) {
        try {
            sseEmitter.send(event);
        } catch (Exception ex) {
            sseEmitter.completeWithError(ex);
        }
    }

    public void removeEmitter(String topic) {
        var sseEmitter = this.connections.get(topic);
        if (sseEmitter == null) {
            return;
        }
        sseEmitter.complete();
        this.connections.remove(topic);
        this.monitoringFileService.removeConsumer(topic);
    }
}
