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
public class RoomDTO  implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private String description;
    private PriceDTO price;
    private Integer capacity;
}
