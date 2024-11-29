package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceDTO {
    private Double value;
    private String unit;
}
