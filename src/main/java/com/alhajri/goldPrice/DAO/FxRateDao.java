package com.alhajri.goldPrice.DAO;

import com.alhajri.goldPrice.DTO.FxRateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FxRateDao {

    private static final String API_URL = "https://cdn.moneyconvert.net/api/latest.json";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static double getUsdPerKwd() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode rates = root.path("rates");

            // get KWD relative to USD
            double kwdPerUsd = rates.path("KWD").asDouble();
            if (kwdPerUsd == 0) throw new RuntimeException("Invalid KWD rate");

            return (double) Math.round((1.0 / kwdPerUsd) * 1000) /1000;// USD per 1 KWD
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch FX rate", e);
        }
    }
}

