package com.example.hotelproject.dto.client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HotelDTO {
    private String hotelId;
    private String name;
    private String chainCode;
    private GeoLocationDTO geoCode;
    private AddressDTO address;
    private DistanceDTO distance;
}
