package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fee {
    private String amount;
    private String currency;
    private String description;
}
