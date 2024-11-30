package com.example.hotelproject.repository;

import com.example.hotelproject.client.FeignConfig;
import com.example.hotelproject.dto.client.HotelOffersResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "amadeusApiV2", url = "https://test.api.amadeus.com/v2", configuration = FeignConfig.class)
public interface AmadeusApiV2Client {
    @GetMapping("/shopping/hotel-offers")
    HotelOffersResponse getHotelOffers(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("hotelIds") String hotelIds,
            @RequestParam("adults") int adults,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam("roomQuantity") int roomQuantity,
            @RequestParam(value = "currency", defaultValue = "USD") String currency,
            @RequestParam(value = "bestRateOnly", defaultValue = "true") boolean bestRateOnly
    );
}
