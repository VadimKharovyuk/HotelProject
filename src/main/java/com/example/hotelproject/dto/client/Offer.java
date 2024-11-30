package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    private String id;
    private Price price;
    private Room room;
    private List<Guest> guests;
}
