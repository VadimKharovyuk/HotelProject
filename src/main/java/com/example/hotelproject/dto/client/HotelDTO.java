package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
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
