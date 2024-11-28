package com.example.hotelproject.controller;

import com.example.hotelproject.dto.client.Hotel;
import com.example.hotelproject.dto.client.HotelSearchResponse;

import com.example.hotelproject.repository.AmadeusApiClient;
import com.example.hotelproject.service.AmadeusAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {
    private final AmadeusApiClient amadeusClient;
    private final AmadeusAuthService authService;

    @GetMapping("/search")
    public ResponseEntity<HotelSearchResponse> searchHotels(
            @RequestParam String cityCode,
            @RequestParam(defaultValue = "10") int radius
    ) {
        try {
            String token = "Bearer " + authService.getAccessToken();
            return ResponseEntity.ok(amadeusClient.searchHotels(token, cityCode, radius));
        } catch (Exception e) {
            log.error("Failed to search hotels", e);
            return ResponseEntity.internalServerError().build();
        }
    }

//    @GetMapping("/{hotelId}")
//    public ResponseEntity<Hotel> getHotelDetails(@PathVariable String hotelId) {
//        try {
//            return amadeusClient.getHotelById(hotelId)
//                    .map(ResponseEntity::ok)
//                    .orElse(ResponseEntity.notFound().build());
//        } catch (Exception e) {
//            log.error("Failed to get hotel details for id {}", hotelId, e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
}