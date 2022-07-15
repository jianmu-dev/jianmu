package dev.jianmu.infrastructure.worker.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolumeHostPath {
    private String id;
    private String name;
    private String path;
    private String type;
}
