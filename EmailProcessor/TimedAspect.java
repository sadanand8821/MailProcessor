package org.example;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

@Aspect
@Component
public class TimedAspect {

    private final MeterRegistry meterRegistry;
    private static final Logger logger = Logger.getLogger(TimedAspect.class.getName());
    private final ConcurrentMap<String, Counter> counterMap = new ConcurrentHashMap<>();

    public TimedAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* org.example..*(..))")
    public Object countMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toLongString();
        logger.info("Intercepting method: " + methodName);

        // Use computeIfAbsent to ensure the counter is created only once and reused subsequently
        Counter counter = counterMap.computeIfAbsent(methodName, key -> Counter.builder("method.counted")
                .tag("method", methodName)
                .register(meterRegistry));

        counter.increment();

        return joinPoint.proceed();
    }
}
