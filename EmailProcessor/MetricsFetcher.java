package org.example.utils;

import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

public class MetricsFetcher {

    public static void main(String[] args) {
        String url = "http://localhost:8080/actuator/metrics/http.server.requests";
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(url, String.class);

        if (jsonResponse != null) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray availableTags = jsonObject.getJSONArray("availableTags");

            for (int i = 0; i < availableTags.length(); i++) {
                JSONObject tag = availableTags.getJSONObject(i);
                if ("uri".equals(tag.getString("tag"))) {
                    JSONArray values = tag.getJSONArray("values");
                    for (int j = 0; j < values.length(); j++) {
                        String endpoint = values.getString(j);
                        System.out.println(endpoint);
                        if (endpoint.contains("actuator") || endpoint.contains("/**")){
                            ;
                        }else {
                            printEndpointCount(endpoint);
                        }
                        //printEndpointCount(endpoint);
                    }
                }
            }
        } else {
            System.out.println("No response received from the Actuator endpoint.");
        }
    }

    private static void printEndpointCount(String endpoint) {
        String url = "http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:" + endpoint;
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(url, String.class);

        if (jsonResponse != null) {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray measurements = jsonObject.getJSONArray("measurements");

            for (int i = 0; i < measurements.length(); i++) {
                JSONObject measurement = measurements.getJSONObject(i);
                if ("COUNT".equals(measurement.getString("statistic"))) {
                    double count = measurement.getDouble("value");
                    System.out.println("Endpoint: " + endpoint + ", Count: " + (int) count);
                }
            }
        } else {
            System.out.println("No response received for endpoint: " + endpoint);
        }
    }
}