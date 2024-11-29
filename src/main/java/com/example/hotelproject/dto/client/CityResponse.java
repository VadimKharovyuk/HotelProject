package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class CityResponse {
    private String name;
    private String iataCode; // Added this field
    private String countryCode;
    @JsonProperty("data")
    private List<CityData> data;
}
