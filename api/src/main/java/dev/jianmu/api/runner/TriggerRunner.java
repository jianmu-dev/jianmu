package dev.jianmu.api.runner;

import dev.jianmu.application.service.TriggerApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Ethan Liu
 * @class TriggerRunner
 * @description TriggerRunner
 * @create 2021-05-24 19:26
 */
@Component
@Slf4j
public class TriggerRunner implements ApplicationRunner {
    private final TriggerApplication triggerApplication;

    public TriggerRunner(TriggerApplication triggerApplication) {
        this.triggerApplication = triggerApplication;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.triggerApplication.startTriggers();
    }
}
