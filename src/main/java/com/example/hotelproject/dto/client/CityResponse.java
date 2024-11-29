package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityResponse {
    private String cityCode;
    private String cityName;
    private String countryCode;
    private GeoCode geoCode;
}
