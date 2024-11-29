package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityData {
    private String iataCode;
    private String name;
    // другие поля
}