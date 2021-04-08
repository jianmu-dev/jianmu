package dev.jianmu.api;

import dev.jianmu.task.aggregate.Worker;
import dev.jianmu.task.repository.WorkerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;

/**
 * @class: WorkerTest
 * @description: Worker测试类
 * @author: Ethan Liu
 * @create: 2021-04-02 13:32
 **/
@SpringBootTest(classes = SpringbootApp.class)
@DisplayName("Worker测试类")
public class WorkerTest {
    @Resource
    private WorkerRepository workerRepository;

    @Test
    void test1() {
        Worker worker = Worker.Builder.aWorker()
                .id("worker9528")
                .name("Worker2")
                .type(Worker.Type.DOCKER)
                .status(Worker.Status.OFFLINE)
                .build();
        this.workerRepository.add(worker);
    }
}
