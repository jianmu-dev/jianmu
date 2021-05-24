package dev.jianmu.trigger.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * @class: PublishJob
 * @description: PublishJob
 * @author: Ethan Liu
 * @create: 2021-05-24 09:29
 **/
public class PublishJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(PublishJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var triggerId = context.getTrigger().getJobDataMap().get("triggerId");
        logger.info("hello: {}", LocalDateTime.now());
        logger.info("triggerId: {}", triggerId);
    }
}
