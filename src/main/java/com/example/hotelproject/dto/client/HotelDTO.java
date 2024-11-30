package com.example.hotelproject.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDTO  implements Serializable {
    private static final long serialVersionUID = 1L;
    private String hotelId;
    private String name;
    private String description;
    private PriceDTO price;
    private List<RoomDTO> rooms;
    private List<String> amenities;
    private AddressDTO address;
    private List<String> photos;
}
