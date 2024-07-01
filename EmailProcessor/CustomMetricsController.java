package org.example;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/custom-metrics")
public class CustomMetricsController {

    @Autowired
    private MeterRegistry meterRegistry;
    private static final Logger logger = Logger.getLogger(CustomMetricsController.class.getName());

    @GetMapping
    public Map<String, Double> getCustomMetrics() {
        logger.info("Fetching custom metrics...");
        Map<String, Double> metrics = meterRegistry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("method.counted"))
                .collect(Collectors.toMap(
                        meter -> meter.getId().getTag("method"),
                        meter -> {
                            Counter counter = meterRegistry.find(meter.getId().getName()).counter();
                            return (counter != null) ? counter.count() : 0.0;
                        }
                ));
        logger.info("Custom metrics: " + metrics);
        return metrics;
    }
}
