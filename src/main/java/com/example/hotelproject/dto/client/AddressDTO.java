package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String street;
    private String city;
    private String country;
    private String postalCode;
}