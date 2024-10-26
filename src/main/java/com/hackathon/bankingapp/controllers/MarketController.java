package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.services.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getAllPrices() {
        Map<String, Double> prices = marketService.getAllPrices();
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/prices/{symbol}")
    public ResponseEntity<Double> getPriceForSymbol(@PathVariable String symbol) {
        Double price = marketService.getPriceForSymbol(symbol);
        return ResponseEntity.ok(price);
    }
}
