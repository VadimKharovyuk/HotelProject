package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private BigDecimal amount;
    private String currency;
}
