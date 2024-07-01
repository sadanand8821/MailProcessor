package org.example.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class MetricsService {

    private static final Logger logger = Logger.getLogger(MetricsService.class.getName());

    @Autowired
    private MeterRegistry meterRegistry;

    public Map<String, Map<String, Double>> getMetrics() {
        Map<String, Map<String, Double>> metrics = new HashMap<>();

        meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equals("method.counted"))
                .forEach(meter -> {
                    String methodName = meter.getId().getTag("method");
                    double count = meter.measure().iterator().next().getValue();

                    Map<String, Double> methodMetrics = metrics.getOrDefault(methodName, new HashMap<>());
                    methodMetrics.put("count", count);
                    metrics.put(methodName, methodMetrics);
                });

        meterRegistry.getMeters().stream()
                .filter(meter -> meter instanceof Timer)
                .filter(meter -> meter.getId().getName().equals("method.timed"))
                .forEach(meter -> {
                    Timer timer = (Timer) meter;
                    String methodName = timer.getId().getTag("method");

                    Map<String, Double> methodMetrics = metrics.getOrDefault(methodName, new HashMap<>());
                    methodMetrics.put("totalTime", timer.totalTime(TimeUnit.MILLISECONDS));
                    methodMetrics.put("maxTime", timer.max(TimeUnit.MILLISECONDS));
                    methodMetrics.put("meanTime", timer.mean(TimeUnit.MILLISECONDS));
                    metrics.put(methodName, methodMetrics);
                });

        logger.info("Metrics Data: " + metrics);
        return metrics;
    }
}