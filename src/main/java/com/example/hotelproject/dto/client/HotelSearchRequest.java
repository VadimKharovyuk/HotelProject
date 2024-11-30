package com.example.hotelproject.dto.client;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelSearchRequest {
    private String destination;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int adults;
    @Min(value = 1, message = "Room count must be at least 1")
    private int roomCount = 1; // Default value of 1
//    private int roomCount;
    private int children;
}
