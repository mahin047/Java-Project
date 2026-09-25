package com.example.demo_java_project.api;

import com.example.demo_java_project.api.dto.HolidayDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class ApiClient {

    private static final String HOLIDAY_API_BASE = "https://date.nager.at/api/v3/PublicHolidays";
    private static final Duration TIMEOUT = Duration.ofSeconds(6);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private final ObjectMapper mapper = JsonService.newMapper();

    /** Public holidays for a country in a given year. Returns an empty list on any failure. */
    public List<HolidayDto> getPublicHolidays(int year, String countryCode) {
        try {
            String url = HOLIDAY_API_BASE + "/" + year + "/" + countryCode;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return List.of();
            }
            return List.of(mapper.readValue(response.body(), HolidayDto[].class));

        } catch (Exception e) {
            System.err.println("Holiday API unavailable: " + e.getMessage());
            return List.of();      // never break booking just because the internet/API is down
        }
    }
}