package com.example.hotelproject.dto.client;

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
    private int roomCount;
    private int children;
}
