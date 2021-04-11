package dev.jianmu.api;

import dev.jianmu.parameter.repository.ParameterRepository;
import dev.jianmu.parameter.service.ParameterDomainService;
import dev.jianmu.task.aggregate.Worker;
import dev.jianmu.task.repository.WorkerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @class: WorkerTest
 * @description: Worker测试类
 * @author: Ethan Liu
 * @create: 2021-04-02 13:32
 **/
@SpringBootTest(classes = SpringbootApp.class)
@DisplayName("Worker测试类")
@ActiveProfiles("dev")
@Transactional
public class WorkerTest {
    @Resource
    private WorkerRepository workerRepository;
    @Resource
    private ParameterDomainService parameterDomainService;
    @Resource
    private ParameterRepository parameterRepository;

    @Test
    void test1() {
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("command", "");
        parameterMap.put("entrypoint", "");
        parameterMap.put("image", "");
        parameterMap.put("network", "bridge");
        parameterMap.put("volume_mounts", "");
        parameterMap.put("volumes", "{\"volumes\":[{\"temp\":{\"id\":\"v-test-2\",\"name\":\"vt2\"}}]}");
        parameterMap.put("working_dir", "");
        var parameters = this.parameterDomainService.createParameters(parameterMap);
        this.parameterRepository.addAll(new ArrayList<>(parameters.values()));
        Worker worker = Worker.Builder.aWorker()
                .id("worker9528")
                .name("Worker2")
                .type(Worker.Type.DOCKER)
                .status(Worker.Status.OFFLINE)
                .build();
        worker.setParameterMap(parameterMap);
        this.workerRepository.add(worker);
    }
}
