package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceVariation {
    private Average average;
    private Changes changes;
}
