package dev.jianmu.trigger.config;

import org.quartz.Scheduler;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.util.Assert;

/**
 * @class: SchedulerConfiguration
 * @description: SchedulerConfiguration
 * @author: Ethan Liu
 * @create: 2021-05-24 08:21
 **/
@Configuration
public class SchedulerConfiguration {
    public class AutowireCapableBeanJobFactory extends SpringBeanJobFactory {
        private final AutowireCapableBeanFactory beanFactory;

        public AutowireCapableBeanJobFactory(AutowireCapableBeanFactory beanFactory) {
            Assert.notNull(beanFactory, "Bean factory must not be null");
            this.beanFactory = beanFactory;
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            final Object jobInstance = super.createJobInstance(bundle);
            this.beanFactory.autowireBean(jobInstance);
            return jobInstance;
        }
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory(ApplicationContext applicationContext) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(new AutowireCapableBeanJobFactory(applicationContext.getAutowireCapableBeanFactory()));
        return schedulerFactoryBean;
    }

    @Bean
    public Scheduler scheduler(ApplicationContext applicationContext) {
        return schedulerFactory(applicationContext).getScheduler();
    }
}
