package com.example.hotelproject.controller;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.HotelDTO;
import com.example.hotelproject.dto.client.HotelSearchRequest;
import com.example.hotelproject.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Controller
@RequestMapping("/search-form")
@RequiredArgsConstructor
public class SearchFormController {
    private final HotelService hotelService;

    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("searchRequest", new HotelSearchRequest());
        return "hotels/search-form";
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
        if (!model.containsAttribute("hotels")) {
            return "redirect:/search-form";
        }
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
}
