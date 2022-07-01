package dev.jianmu.api.oauth2_api.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author huangxi
 * @class ApplicationContextUtils
 * @description spring回调
 * @create 2021-06-30 14:08
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    /**
     * 保留下来工厂
     */
    private static ApplicationContext applicationContext;

    /**
     * 提供根据bean name在工厂中获取对象的方法
     */
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 提供根据bean type在工厂中获取对象的方法
     */
    public static <T> T getBean(Class<T> beanType) {
        return applicationContext.getBean(beanType);
    }

    /**
     * 在非spring管理的类中也能获取Spring中的bean
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;
    }
}