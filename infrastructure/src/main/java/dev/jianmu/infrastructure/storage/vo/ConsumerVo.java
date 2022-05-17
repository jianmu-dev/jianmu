package dev.jianmu.infrastructure.storage.vo;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

@Getter
@Setter
public class ConsumerVo {
    private String connectionId;
    private BiConsumer<Path, AtomicLong> consumer;
    private AtomicLong counter = new AtomicLong();

    public ConsumerVo(String connectionId, BiConsumer<Path, AtomicLong> consumer) {
        this.connectionId = connectionId;
        this.consumer = consumer;
    }
}
