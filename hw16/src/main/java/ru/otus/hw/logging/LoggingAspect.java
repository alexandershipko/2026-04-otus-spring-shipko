package ru.otus.hw.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(ru.otus.hw.logging.TrackTime)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        var result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - startTime;

        log.info("Time taken by {} is {} milliseconds", joinPoint, timeTaken);

        return result;
    }

}
