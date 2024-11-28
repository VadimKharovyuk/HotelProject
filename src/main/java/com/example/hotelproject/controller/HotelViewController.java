package com.example.hotelproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HotelViewController {
    @GetMapping("/hotels")
    public String showHotelSearch() {
        return "hotels/search";
    }
}