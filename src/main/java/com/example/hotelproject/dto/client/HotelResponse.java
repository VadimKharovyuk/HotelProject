package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelResponse {
    private String hotelId;
    private String name;
    private Address address;
    private Double rating;
    private String description;
    private PriceInfo price;
    private List<String> amenities;
    private Contact contact;
    private List<Media> images;
    private List<Room> rooms;

    @Data
    public static class PriceInfo {
        private BigDecimal amount;
        private String currency;
        private String type;
    }
}