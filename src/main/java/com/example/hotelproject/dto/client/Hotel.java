package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class Hotel {
    private String hotelId;
    private String name;
    private Contact contact;
    private Double stars;
    private Rate rate;
    private List<Room> rooms;
    private List<String> amenities;
    private Address address;
    private List<Media> mediaUrls;
}