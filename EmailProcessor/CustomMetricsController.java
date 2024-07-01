package org.example;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/custom-metrics")
public class CustomMetricsController {

    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping
    public Map<String, Double> getCustomMetrics() {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("method.timed"))
                .collect(Collectors.toMap(
                        meter -> meter.getId().getName(),
                        meter -> meterRegistry.find(meter.getId().getName())
                                .timer()
                                .mean(TimeUnit.MILLISECONDS)
                ));
    }
}