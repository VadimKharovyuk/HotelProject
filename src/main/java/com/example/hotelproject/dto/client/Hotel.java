package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    private String hotelId;
    private String name;
    private String description;
    private List<String> amenities;
    private Address address;
    private Price price;
    private List<Room> rooms;
    private List<String> photos;
}