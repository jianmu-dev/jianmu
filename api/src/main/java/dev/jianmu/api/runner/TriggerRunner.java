package dev.jianmu.api.runner;

import dev.jianmu.application.service.TriggerApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @class: TriggerRunner
 * @description: TriggerRunner
 * @author: Ethan Liu
 * @create: 2021-05-24 19:26
 **/
@Component
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
