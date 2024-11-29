package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Room {
    private String roomId;
    private String type;
    private String description;
    private Integer capacity;
    private Rate rate;
    private List<String> amenities;
    private List<Media> images;
    private Integer availableCount;
    private String bedType;
    private String viewType;
    private Boolean smoking;
    private String cancellationPolicy;
}