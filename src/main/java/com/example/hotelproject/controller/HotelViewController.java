package com.example.hotelproject.controller;

import com.example.hotelproject.dto.client.CityResponse;
import com.example.hotelproject.dto.client.HotelDTO;
import com.example.hotelproject.repository.AmadeusApiClient;
import com.example.hotelproject.service.AmadeusAuthService;
import com.example.hotelproject.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HotelViewController {
  private final HotelService hotelService;


    @GetMapping("/hotels")
    public String showHotels(@RequestParam String cityCode, Model model) {
        List<HotelDTO> hotels = hotelService.searchHotels(cityCode, 5);
        model.addAttribute("hotels", hotels);
        return "hotels/list";
    }
}