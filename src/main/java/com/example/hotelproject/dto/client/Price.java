package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {
    private String total;
    private String base;
    private String currency;
    private List<PriceVariation> variations;
}