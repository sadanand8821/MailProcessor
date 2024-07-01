package org.example;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimedAspect {

    private final MeterRegistry meterRegistry;

    public TimedAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* org.example..*(..))") // Adjust the package name as needed
    public Object timeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer timer = Timer.builder("method.timed")
                .tag("method", joinPoint.getSignature().toShortString())
                .register(meterRegistry);

        return timer.recordCallable(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
    }
}