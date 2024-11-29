package com.example.hotelproject.repository;

import com.example.hotelproject.client.FeignConfig;
import com.example.hotelproject.dto.client.CityResponse;
import com.example.hotelproject.dto.client.HotelResponse;
import com.example.hotelproject.dto.client.HotelSearchResponse;
import com.example.hotelproject.dto.client.LocationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;


@FeignClient(name = "amadeusApi", url = "https://test.api.amadeus.com/v1", configuration = FeignConfig.class)
public interface AmadeusApiClient {
    @GetMapping("/reference-data/locations/hotels/by-city")
    HotelSearchResponse searchHotels(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("cityCode") String cityCode,
            @RequestParam("radius") Integer radius,
            @RequestParam("ratings") String ratings,
            @RequestParam(value = "hotelSource", required = false) String hotelSource
    );


    @GetMapping(value = "/reference-data/locations")
    LocationResponse searchCities(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("keyword") String query,
            @RequestParam("subType") String subType
    );
    @GetMapping("/reference-data/locations/hotels/{hotelId}")
    HotelResponse getHotelDetails(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String hotelId
    );

}