package com.example.hotelproject.dto.client;

import lombok.Data;

import java.util.List;

@Data
public class HotelSearchResponse {
    private List<Hotel> data;
    private Meta meta;
}
