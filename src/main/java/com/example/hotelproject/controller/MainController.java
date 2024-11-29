package com.example.hotelproject.controller;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.AddressDTO;
import com.example.hotelproject.dto.client.HotelDTO;
import com.example.hotelproject.dto.client.HotelSearchRequest;
import com.example.hotelproject.dto.client.PriceDTO;
import com.example.hotelproject.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
    private final HotelService hotelService;

    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("searchRequest", new HotelSearchRequest());
        return "home";
    }

    @PostMapping("/submit")
    public String submitSearchForm(@ModelAttribute HotelSearchRequest request, RedirectAttributes redirectAttributes) {
        log.info("Received search request: {}", request);

        try {
            validateRequest(request);
            log.debug("Request validated successfully");

            // Поиск отелей через сервис
            List<HotelDTO> hotels = hotelService.searchHotels(request);
            redirectAttributes.addFlashAttribute("hotels", hotels);
            redirectAttributes.addFlashAttribute("searchRequest", request);

            return "redirect:/results";

        } catch (ResourceNotFoundException e) {
            log.error("City not found", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/search-form";
        } catch (Exception e) {
            log.error("Error processing search request", e);
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при поиске отелей");
            return "redirect:/search-form";
        }
    }

    @GetMapping("/results")
    public String showResults(Model model) {
        List<HotelDTO> hotels = (List<HotelDTO>)
                model.getAttribute("hotels");
        if (hotels == null) {
            return "redirect:/search-form";
        }

//        hotels = hotelService.enrichHotelsWithDetails(hotels);

        model.addAttribute("hotels", hotels);
        model.addAttribute("searchRequest", model.getAttribute("searchRequest"));

        return "hotels/results";
    }

    private void validateRequest(HotelSearchRequest request) {
        List<String> errors = new ArrayList<>();

        if (StringUtils.isEmpty(request.getDestination())) {
            errors.add("Укажите место назначения");
        }
        if (request.getCheckIn() == null) {
            errors.add("Выберите дату заезда");
        }
        if (request.getCheckOut() == null) {
            errors.add("Выберите дату выезда");
        }
        if (request.getAdults() == 0) {
            errors.add("Выберите количество гостей");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
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