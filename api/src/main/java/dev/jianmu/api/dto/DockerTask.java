package dev.jianmu.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @class: DockerTask
 * @description: Docker类型任务DTO
 * @author: Ethan Liu
 * @create: 2021-04-04 16:14
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DockerTask {
    private List<TasksEntity> Tasks;
    private List<VolumesEntity> volumes;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class TasksEntity {
        private String id;
        private String name;
        private String image;
        private String network;
        private String working_dir;
        private List<String> entrypoint;
        private List<String> args;
        private Map<String, String> environment;
        private List<VolumeMountsEntity> volume_mounts;

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        @Builder
        public static class VolumeMountsEntity {
            private String name;
            private String path;
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class VolumesEntity {
        private TempEntity temp;

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        @Builder
        public static class TempEntity {
            private String id;
            private String name;
        }
    }
}
