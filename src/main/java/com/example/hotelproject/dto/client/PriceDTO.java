package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceDTO {
    private BigDecimal amount;
    private String currency;
}
