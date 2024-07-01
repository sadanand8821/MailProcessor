package org.example.controller;

import org.example.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.logging.Logger;

@Controller
public class MetricsController {

    private static final Logger logger = Logger.getLogger(MetricsController.class.getName());

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/metrics")
    public String getMetrics(Model model) {
        Map<String, Map<String, Double>> metrics = metricsService.getMetrics();
        model.addAttribute("metrics", metrics);

        logger.info("Metrics: " + metrics);

        return "metricsView";
    }
}