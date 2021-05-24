package dev.jianmu.trigger.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

/**
 * @class: PublishJob
 * @description: PublishJob
 * @author: Ethan Liu
 * @create: 2021-05-24 09:29
 **/
public class PublishJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var triggerId = context.getTrigger().getJobDataMap().get("triggerId");
        System.out.println("hello:" + LocalDateTime.now());
        System.out.println("triggerId: " + triggerId);
    }
}
