package com.example.hotelproject.controller;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.HotelDTO;
import com.example.hotelproject.dto.client.HotelSearchRequest;
import com.example.hotelproject.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final HotelService hotelService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("searchRequest", new HotelSearchRequest());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "home";
    }

    @GetMapping("/search")
    public String processSearch(@ModelAttribute HotelSearchRequest request, RedirectAttributes redirectAttributes) {
        try {
            if (request.getRoomCount() == 0) {
                request.setRoomCount(1);
            }
            List<HotelDTO> hotels = hotelService.searchHotels(request);
            redirectAttributes.addFlashAttribute("hotels", hotels);
            redirectAttributes.addFlashAttribute("searchRequest", request);
            return "redirect:/results";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }



    @GetMapping("/results")
    public String showResults(@ModelAttribute("searchRequest") HotelSearchRequest request,
                              @ModelAttribute("hotels") List<HotelDTO> hotels,
                              Model model) {
        // Переносим данные из RedirectAttributes (если нужно вручную отобразить страницу)
        model.addAttribute("searchRequest", request);
        model.addAttribute("hotels", hotels);
        return "hotels/results";
    }



    @GetMapping("/results/filter")
    public String filterResults(@RequestParam(required = false) Double minPrice,
                                @RequestParam(required = false) Double maxPrice,
                                @RequestParam(required = false) Integer minRating,
                                @ModelAttribute("hotels") List<HotelDTO> hotels,
                                Model model) {
        List<HotelDTO> filteredHotels = hotels.stream()
                .filter(hotel -> (minPrice == null || hotel.getPrice().getAmount().doubleValue() >= minPrice)
                        && (maxPrice == null || hotel.getPrice().getAmount().doubleValue() <= maxPrice)
                        && (minRating == null || hotel.getRating() >= minRating))
                .collect(Collectors.toList());

        model.addAttribute("hotels", filteredHotels);
        return "hotels/results";
    }

    @GetMapping("/{hotelId}/details")
    public String showHotelDetails(@PathVariable String hotelId, Model model) {
        // Для будущей реализации детальной страницы отеля
        return "hotels/details";
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Произошла ошибка: " + e.getMessage());
        return "redirect:/search";
    }
}