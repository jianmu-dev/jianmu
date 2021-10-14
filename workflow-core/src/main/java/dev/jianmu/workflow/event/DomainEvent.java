package dev.jianmu.workflow.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface DomainEvent extends Serializable {
    LocalDateTime getOccurredTime();
    String getIdentify();
}
