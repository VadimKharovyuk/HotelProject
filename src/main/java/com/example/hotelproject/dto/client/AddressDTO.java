package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    private String cityName;
    private String countryCode;
}