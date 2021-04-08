package dev.jianmu.infrastructure.annotation;

import dev.jianmu.infrastructure.exception.DBException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

/**
 * @class: TryAgainAspect
 * @description: 乐观锁重试切面
 * @author: Ethan Liu
 * @create: 2021-03-22 13:44
 **/
@Aspect
@Configuration
public class TryAgainAspect implements Ordered {

    // 默认重试3次
    private static final int DEFAULT_MAX_RETRIES = 3;

    private int order = 1;

    @Override
    public int getOrder() {
        return this.order;
    }

    @Pointcut("@annotation(IsTryAgain)")
    public void retryOnOptFailure() {
        // pointcut mark
    }

    @Around("retryOnOptFailure()")
    @Transactional(rollbackFor = Exception.class)
    public Object doRetry(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        do {
            numAttempts++;
            try {
                Object proceed = pjp.proceed();
                System.out.println("==重试成功==");
                return proceed;
            } catch (DBException.OptimisticLocking e) {
                if (numAttempts > DEFAULT_MAX_RETRIES) {
                    // 如果大于 默认的重试机制 次数，抛出异常
                    throw new RuntimeException("超过重试次数");
                } else {
                    //如果 没达到最大的重试次数，将再次执行
                    System.out.println("=====正在重试====="+numAttempts+"次");
                }
            }
        }while (numAttempts <= DEFAULT_MAX_RETRIES);

        return null;
    }
}
