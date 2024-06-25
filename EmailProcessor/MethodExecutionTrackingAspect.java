package org.example.aspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class MethodExecutionTrackingAspect {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counters = new HashMap<>();

    public MethodExecutionTrackingAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Pointcut("execution(* com.example..*(..))")  // Adjust the package as necessary
    public void allMethods() {}

    @After("allMethods()")
    public void incrementCounter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        counters.computeIfAbsent(methodName, name -> meterRegistry.counter("method.execution.count", "method", name))
                .increment();
    }
}