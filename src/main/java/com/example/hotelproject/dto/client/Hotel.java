package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Hotel {
    private String hotelId;
    private String name;
    private String chainCode;
    private GeoCode geoCode;
    private Address address;
    @JsonProperty("distance")
    private DistanceInfo distance;
}