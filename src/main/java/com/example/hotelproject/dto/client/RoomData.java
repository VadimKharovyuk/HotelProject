package com.example.hotelproject.dto.client;

import lombok.Data;

import java.util.List;

@Data
public class RoomData {
    private String type;
    private int availableCount;
    private List<String> amenities;
    private PriceData price;
}
