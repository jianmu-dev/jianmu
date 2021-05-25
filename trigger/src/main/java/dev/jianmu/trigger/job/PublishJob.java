package dev.jianmu.trigger.job;

import dev.jianmu.trigger.event.TriggerEvent;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @class: PublishJob
 * @description: PublishJob
 * @author: Ethan Liu
 * @create: 2021-05-24 09:29
 **/
@Component
public class PublishJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(PublishJob.class);
    private final ApplicationEventPublisher publisher;

    public PublishJob(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var triggerId = context.getTrigger().getJobDataMap().get("triggerId").toString();
        logger.info("triggerId: {}", triggerId);
        var evt = TriggerEvent.Builder.aTriggerEvent().triggerId(triggerId).build();
        logger.info("publishEvent at: {}", LocalDateTime.now());
        this.publisher.publishEvent(evt);
    }
}
