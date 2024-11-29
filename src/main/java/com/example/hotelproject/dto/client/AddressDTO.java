package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    private String street;
    private String cityName;
    private String country;
    private Double latitude;
    private Double longitude;
    private String coordinates;
}