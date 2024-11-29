package com.example.hotelproject.repository;

import com.example.hotelproject.client.FeignConfig;
import com.example.hotelproject.dto.client.CityResponse;
import com.example.hotelproject.dto.client.HotelSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "amadeusApi", url = "https://test.api.amadeus.com/v1", configuration = FeignConfig.class)
public interface AmadeusApiClient {
    @GetMapping(value = "/reference-data/locations/hotels/by-city")
    HotelSearchResponse searchHotels(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("cityCode") String cityCode,
            @RequestParam("radius") int radius,
            @RequestParam("roomCount") int roomCount,
            @RequestParam("adults") int adults,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate
    );

    @GetMapping(value = "/reference-data/locations")
    List<CityResponse> searchCities(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("keyword") String query,
            @RequestParam("subType") String subType
    );

}