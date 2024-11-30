package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOffer {
    private String hotelId;
    private List<Offer> offers;
}
