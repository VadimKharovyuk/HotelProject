package com.example.hotelproject.service;

import com.example.hotelproject.dto.client.*;
import com.example.hotelproject.repository.AmadeusApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final AmadeusApiClient amadeusClient;
    private final AmadeusAuthService authService;

    public List<HotelDTO> searchHotels(String cityCode, int radius) {
        String token = "Bearer " + authService.getAccessToken();
        return amadeusClient.searchHotels(token, cityCode, radius)
                .getData().stream()
                .map(hotel -> HotelDTO.builder()
                        .hotelId(hotel.getHotelId())
                        .name(hotel.getName())
                        .chainCode(hotel.getChainCode())
                        .geoCode(GeoLocationDTO.builder()
                                .latitude(hotel.getGeoCode().getLat())
                                .longitude(hotel.getGeoCode().getLon())
                                .build())
                        .address(AddressDTO.builder()
                                .cityName(hotel.getAddress().getCityName())
                                .countryCode(hotel.getAddress().getCountryCode())
                                .build())
                        .distance(DistanceDTO.builder()
                                .value(hotel.getDistance().getValue())
                                .unit(hotel.getDistance().getUnit())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }


}
