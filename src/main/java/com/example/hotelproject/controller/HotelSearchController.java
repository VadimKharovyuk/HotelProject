package com.example.hotelproject.controller;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.AddressDTO;
import com.example.hotelproject.dto.client.HotelDTO;
import com.example.hotelproject.dto.client.HotelSearchRequest;
import com.example.hotelproject.dto.client.PriceDTO;
import com.example.hotelproject.service.HotelService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class HotelSearchController {
    private final HotelService hotelService;
    private static final String HOTELS_SESSION_KEY = "searchResults";
    private static final String SEARCH_REQUEST_SESSION_KEY = "lastSearchRequest";
    private static final int DEFAULT_PAGE_SIZE = 12;

    @GetMapping
    public String showSearchForm(Model model) {
        model.addAttribute("searchRequest", new HotelSearchRequest());
        return "home";
    }



    @PostMapping("/search")
    public String processSearch(@Valid @ModelAttribute HotelSearchRequest request,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        log.info("Processing search request: {}", request);

        try {
            List<HotelDTO> hotels = hotelService.searchHotels(request);

            if (hotels.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning",
                        String.format("Отели в городе '%s' не найдены", request.getDestination()));
                return "redirect:/search-form";
            }

            // Сохраняем результаты в сессии
            session.setAttribute(HOTELS_SESSION_KEY, hotels);
            session.setAttribute(SEARCH_REQUEST_SESSION_KEY, request);

            log.debug("Found {} hotels matching criteria", hotels.size());
            return "redirect:/results";

        } catch (ResourceNotFoundException e) {
            log.error("City not found: {}", request.getDestination(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/search-form";
        } catch (Exception e) {
            log.error("Error processing search request for city: {}", request.getDestination(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Произошла ошибка при поиске отелей. Пожалуйста, попробуйте снова.");
            return "redirect:/search-form";
        }
    }

    @GetMapping("/results")
    public String showResults(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "price_asc") String sort,
                              @RequestParam(required = false) BigDecimal minPrice,
                              @RequestParam(required = false) BigDecimal maxPrice,
                              @RequestParam(required = false) List<String> amenities,
                              Model model,
                              HttpSession session) {

        // Получаем данные из сессии
        List<HotelDTO> hotels = (List<HotelDTO>) session.getAttribute(HOTELS_SESSION_KEY);
        HotelSearchRequest searchRequest = (HotelSearchRequest) session.getAttribute(SEARCH_REQUEST_SESSION_KEY);

        if (hotels == null || searchRequest == null) {
            return "redirect:/";
        }

        // Применяем фильтры
        List<HotelDTO> filteredHotels = filterHotels(hotels, minPrice, maxPrice, amenities);

        // Сортируем результаты
        sortHotels(filteredHotels, sort);

        // Пагинация
        int totalHotels = filteredHotels.size();
        int totalPages = (int) Math.ceil((double) totalHotels / DEFAULT_PAGE_SIZE);
        int startIndex = page * DEFAULT_PAGE_SIZE;
        int endIndex = Math.min(startIndex + DEFAULT_PAGE_SIZE, totalHotels);

        List<HotelDTO> pagedHotels = filteredHotels.subList(startIndex, endIndex);

        // Собираем доступные удобства для фильтров
        Set<String> availableAmenities = hotels.stream()
                .flatMap(hotel -> hotel.getAmenities().stream())
                .collect(Collectors.toSet());

        // Добавляем все необходимые атрибуты в модель
        model.addAttribute("hotels", pagedHotels);
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalHotels", totalHotels);
        model.addAttribute("sort", sort);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedAmenities", amenities);
        model.addAttribute("availableAmenities", availableAmenities);

        return "hotels/results";
    }

    @GetMapping("/hotel/{hotelId}")
    public String showHotelDetails(@PathVariable String hotelId, Model model, HttpSession session) {
        List<HotelDTO> hotels = (List<HotelDTO>) session.getAttribute(HOTELS_SESSION_KEY);

        if (hotels != null) {
            HotelDTO hotel = hotels.stream()
                    .filter(h -> h.getHotelId().equals(hotelId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Отель не найден"));

            model.addAttribute("hotel", hotel);
            return "hotels/details";
        }

        throw new ResourceNotFoundException("Отель не найден");
    }

    private List<HotelDTO> filterHotels(List<HotelDTO> hotels,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        List<String> amenities) {
        return hotels.stream()
                .filter(hotel -> {
                    boolean priceMatch = true;
                    if (hotel.getPrice() != null) {
                        if (minPrice != null) {
                            priceMatch = hotel.getPrice().getAmount().compareTo(minPrice) >= 0;
                        }
                        if (maxPrice != null) {
                            priceMatch = priceMatch && hotel.getPrice().getAmount().compareTo(maxPrice) <= 0;
                        }
                    }

                    boolean amenitiesMatch = true;
                    if (amenities != null && !amenities.isEmpty()) {
                        amenitiesMatch = hotel.getAmenities().containsAll(amenities);
                    }

                    return priceMatch && amenitiesMatch;
                })
                .collect(Collectors.toList());
    }

    private void sortHotels(List<HotelDTO> hotels, String sort) {
        switch (sort) {
            case "price_asc":
                hotels.sort(Comparator.comparing(hotel ->
                        hotel.getPrice() != null ? hotel.getPrice().getAmount() : BigDecimal.ZERO));
                break;
            case "price_desc":
                hotels.sort((h1, h2) -> {
                    BigDecimal price1 = h1.getPrice() != null ? h1.getPrice().getAmount() : BigDecimal.ZERO;
                    BigDecimal price2 = h2.getPrice() != null ? h2.getPrice().getAmount() : BigDecimal.ZERO;
                    return price2.compareTo(price1);
                });
                break;
            case "name_asc":
                hotels.sort(Comparator.comparing(HotelDTO::getName));
                break;
            case "name_desc":
                hotels.sort(Comparator.comparing(HotelDTO::getName).reversed());
                break;
            default:
                // По умолчанию сортируем по возрастанию цены
                hotels.sort(Comparator.comparing(hotel ->
                        hotel.getPrice() != null ? hotel.getPrice().getAmount() : BigDecimal.ZERO));
        }
    }
}