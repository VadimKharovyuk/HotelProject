package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HotelDTO {
    private String hotelId;
    private String name;
    private String description;
    private Double rating;
    private PriceDTO price;
    private List<RoomDTO> rooms;
    private List<String> amenities;
    private AddressDTO address;
    private List<String> photos;
}
