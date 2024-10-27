package com.hackathon.bankingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssetInfoDto {
    private String symbol;
    private double quantity;
}
