package com.alhajri.goldPrice.DAO;

import com.alhajri.goldPrice.DTO.MetalPriceDao;
import com.alhajri.goldPrice.DTO.MetalPricesResponse;
import com.alhajri.goldPrice.entity.MetalCfdResult;
import com.alhajri.goldPrice.entity.MetalPriceDto;
import com.alhajri.goldPrice.util.GoldCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
@Repository
public class MetalPriceDaoImpl implements MetalPriceDao {

    private static final String API_URL =
            "https://api.daralsabaek.com/api/goldAndFundBalance/getMetalPrices";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final double usdPerKwd;

    public MetalPriceDaoImpl() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // fetch USD/KWD once at startup (can refresh periodically if needed)
        this.usdPerKwd = FxRateDao.getUsdPerKwd();
    }

    /**
     * Async fetch of metal prices
     */
    public CompletableFuture<List<MetalPriceDto>> getMetalPricesAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        MetalPricesResponse apiResponse =
                                objectMapper.readValue(body, MetalPricesResponse.class);

                        if (!apiResponse.isSuccess()) {
                            throw new RuntimeException("API error: " + apiResponse.getMessage());
                        }

                        return apiResponse.getResult();

                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse metal prices", e);
                    }
                });
    }

    /**
     * Async fetch of metal prices + CFD calculation
     */
    public CompletableFuture<List<MetalCfdResult>> getMetalPricesWithCfdAsync() {
        return getMetalPricesAsync()
                .thenApply(metals -> metals.stream()
                        .map(metal -> new MetalCfdResult(
                                metal.getMetalType(),
                                metal.getBuyPrice24(),
                                GoldCalculator.toCfd(
                                        metal.getBuyPrice24().doubleValue(),
                                        usdPerKwd
                                )
                        ))
                        .collect(Collectors.toList())
                );
    }


    public List<MetalCfdResult> getMetalPricesWithCfd() throws Exception {

        List<MetalPriceDto> metals = getMetalPrices();

        return metals.stream()
                .map(metal -> new MetalCfdResult(
                        metal.getMetalType(),
                        metal.getBuyPrice24(),
                        GoldCalculator.toCfd(
                                metal.getBuyPrice24().doubleValue(),
                                usdPerKwd
                        )
                ))
                .collect(Collectors.toList());
    }

    public List<MetalPriceDto> getMetalPrices() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        MetalPricesResponse apiResponse =
                objectMapper.readValue(response.body(), MetalPricesResponse.class);
        if (!apiResponse.isSuccess()) {
            throw new RuntimeException("API error: " + apiResponse.getMessage());
        }
        return apiResponse.getResult();
    }



    public Double getUsdPerKwd(){
        return this.usdPerKwd;
    }
}
