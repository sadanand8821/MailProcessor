package org.example.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Aspect
@Component
public class TimedAspect {

    private static final Logger logger = Logger.getLogger(TimedAspect.class.getName());
    private final ConcurrentMap<String, Counter> counterMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timerMap = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    public TimedAspect(@Lazy MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* org.example.controller..*(..))")
    public Object countMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toLongString();
        logger.info("Intercepting method: " + methodName);

        Counter counter = counterMap.computeIfAbsent(methodName, key -> Counter.builder("method.counted")
                .tag("method", methodName)
                .register(meterRegistry));

        Timer timer = timerMap.computeIfAbsent(methodName, key -> Timer.builder("method.timed")
                .tag("method", methodName)
                .register(meterRegistry));

        counter.increment();
        Object result;
        long startTime = System.nanoTime();
        try {
            result = joinPoint.proceed();
        } finally {
            timer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            logger.info("Count for " + methodName + " incremented to " + counter.count());
        }
        return result;
    }
}
