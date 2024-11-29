package com.example.hotelproject.dto.client;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceData {
    private BigDecimal amount;
    private String currency;
}
