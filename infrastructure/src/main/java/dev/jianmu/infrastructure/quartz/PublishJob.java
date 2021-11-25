package dev.jianmu.infrastructure.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @class PublishJob
 * @description PublishJob
 * @author Ethan Liu
 * @create 2021-05-24 09:29
*/
@Component
public class PublishJob implements Job {
    private final ApplicationEventPublisher publisher;

    public PublishJob(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var triggerId = context.getTrigger().getJobDataMap().get("triggerId").toString();
        var evt = CronTriggerEvent.builder().triggerId(triggerId).build();
        this.publisher.publishEvent(evt);
    }
}
