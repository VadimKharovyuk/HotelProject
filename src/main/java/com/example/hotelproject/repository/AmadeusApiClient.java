package com.example.hotelproject.repository;//package com.example.hotelproject.repository;
import com.example.hotelproject.client.FeignConfig;
import com.example.hotelproject.dto.client.HotelOffersResponse;
import com.example.hotelproject.dto.client.HotelSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "amadeusApi", url = "https://test.api.amadeus.com", configuration = FeignConfig.class)
public interface AmadeusApiClient {
    @GetMapping("/v1/reference-data/locations/hotels/by-city")
    HotelSearchResponse searchHotels(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("cityCode") String cityCode,
            @RequestParam("radius") Integer radius,
            @RequestParam("ratings") String ratings,
            @RequestParam(value = "hotelSource", required = false) String hotelSource
    );


    // Второй API endpoint на v2
    @GetMapping("/v2/shopping/hotel-offers")
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