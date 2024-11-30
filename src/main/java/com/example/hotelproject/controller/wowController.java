package com.example.hotelproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/wow")
@Controller
public class wowController {

    @GetMapping
    public String wow() {
        return "wow";
    }


}
