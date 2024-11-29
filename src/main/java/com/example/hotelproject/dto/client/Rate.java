package com.example.hotelproject.dto.client;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Rate {
    private BigDecimal amount;
    private String currency;
}
