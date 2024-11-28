package com.example.hotelproject.repository;

import com.example.hotelproject.dto.client.HotelSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "amadeusApi", url = "https://test.api.amadeus.com/v1")
public interface AmadeusApiClient {
    @GetMapping(value = "/reference-data/locations/hotels/by-city",
            headers = "Authorization={authorization}")
    HotelSearchResponse searchHotels(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("cityCode") String cityCode,
            @RequestParam("radius") int radius
    );
}