package com.hackathon.bankingapp.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MarketService {

    private static final Map<String, Double> PRICES = new HashMap<>();

    static {
        PRICES.put("AAPL", 81.05);
        PRICES.put("GOOGL", 1082.33);
        PRICES.put("TSLA", 75.71);
        PRICES.put("AMZN", 119.0);
        PRICES.put("MSFT", 161.23);
        PRICES.put("NFLX", 427.81);
        PRICES.put("FB", 11.68);
        PRICES.put("BTC", 8304.25);
        PRICES.put("ETH", 91.54);
        PRICES.put("XRP", 4.26);
        PRICES.put("GOLD", 1162.48);
        PRICES.put("SILVER", 4.24);
    }

    public Map<String, Double> getAllPrices() {
        return new HashMap<>(PRICES);
    }

    public Double getPriceForSymbol(String symbol) {
        return PRICES.get(symbol.toUpperCase());
    }
}
