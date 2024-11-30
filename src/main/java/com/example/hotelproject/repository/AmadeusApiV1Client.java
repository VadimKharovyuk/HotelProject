package com.example.hotelproject.repository;

import com.example.hotelproject.client.FeignConfig;
import com.example.hotelproject.dto.client.HotelSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "amadeusApiV1", url = "https://test.api.amadeus.com/v1", configuration = FeignConfig.class)
public interface AmadeusApiV1Client {
    @GetMapping("/reference-data/locations/hotels/by-city")
    HotelSearchResponse searchHotels(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("cityCode") String cityCode,
            @RequestParam("radius") Integer radius,
            @RequestParam("ratings") String ratings,
            @RequestParam(value = "hotelSource", required = false) String hotelSource
    );
}
