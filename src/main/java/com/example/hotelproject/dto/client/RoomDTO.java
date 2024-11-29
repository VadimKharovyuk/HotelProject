package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomDTO {
    private String type;
    private Integer quantity;
    private PriceDTO price;
    private List<String> amenities;
}
