package com.hackathon.bankingapp.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketService {

    private final RestTemplate restTemplate;
    private static final String PRICE_API_URL = "https://faas-lon1-917a94a7.doserverless.co/api/v1/web/fn-e0f31110-7521-4cb9-86a2-645f66eefb63/default/market-prices-simulator";

    public MarketService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Double> getAllPrices() {
        return fetchPricesFromApi().orElse(Collections.emptyMap());
    }

    public Double getPriceForSymbol(String symbol) {
        return fetchPricesFromApi().map(prices -> prices.get(symbol.toUpperCase())).orElse(null);
    }

    private Optional<Map<String, Double>> fetchPricesFromApi() {
        try {
            return Optional.ofNullable(restTemplate.getForObject(PRICE_API_URL, Map.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
