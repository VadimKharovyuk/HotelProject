package com.example.hotelproject.service;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.*;
import com.example.hotelproject.repository.AmadeusApiClient;
import com.example.hotelproject.service.AmadeusAuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {
    private final AmadeusApiClient amadeusClient;
    private final AmadeusAuthService authService;

    public List<HotelDTO> searchHotels(HotelSearchRequest request) {
        String token = authService.getAccessToken();
        int roomCount = Math.max(1, request.getRoomCount());

        LocationResponse locationResponse = amadeusClient.searchCities(
                "Bearer " + token,
                request.getDestination(),
                "CITY"
        );

        if (locationResponse == null || locationResponse.getData() == null) {
            throw new ResourceNotFoundException("Не удалось найти информацию о городе");
        }

        CityData city = locationResponse.getData().stream()
                .filter(c -> c != null && "CITY".equals(c.getSubType()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Город не найден"));

        HotelSearchResponse response = amadeusClient.searchHotels(
                "Bearer " + token,
                city.getIataCode(),
                20,
                "1,2,3,4,5",
                "ALL"
        );

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return mapToHotelDTOs(response);
    }

    private List<HotelDTO> mapToHotelDTOs(HotelSearchResponse response) {
        return Optional.ofNullable(response.getData())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(hotel -> HotelDTO.builder()
                        .hotelId(hotel.getHotelId())
                        .name(hotel.getName())
                        .description(Optional.ofNullable(hotel.getContact())
                                .map(Contact::getDescription)
                                .orElse(null))
                        .rating(hotel.getStars())
                        .price(mapToPrice(hotel.getRate()))
                        .rooms(mapToRooms(hotel.getRooms()))
                        .amenities(Optional.ofNullable(hotel.getAmenities())
                                .orElse(Collections.emptyList()))
                        .address(mapToAddress(hotel.getAddress()))
                        .photos(mapMediaToPhotos(hotel.getMediaUrls()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> mapMediaToPhotos(List<Media> mediaList) {
        return Optional.ofNullable(mediaList)
                .map(list -> list.stream()
                        .map(Media::getUri)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }


    private List<RoomDTO> mapToRooms(List<Room> rooms) {
        if (rooms == null) return Collections.emptyList();

        return rooms.stream()
                .filter(room -> room != null && room.getRate() != null)
                .map(room -> RoomDTO.builder()
                        .type(room.getType())
                        .price(mapToPrice(room.getRate()))
                        .amenities(Optional.ofNullable(room.getAmenities()).orElse(Collections.emptyList()))
                        .quantity(room.getAvailableCount())
                        .build())
                .collect(Collectors.toList());
    }
    private PriceDTO mapToPrice(Rate rate) {
        if (rate == null) return null;
        return PriceDTO.builder()
                .amount(rate.getAmount())
                .currency(rate.getCurrency())
                .build();
    }



    private AddressDTO mapToAddress(Address address) {
        return AddressDTO.builder()
                .street(address.getStreet())
                .cityName(address.getCity())
                .country(address.getCountry())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .coordinates(address.getLatitude() + "," + address.getLongitude())
                .build();
    }


    public List<HotelDTO> enrichHotelsWithDetails(List<HotelDTO> hotels) {
        return hotels.stream()
                .map(this::enrichHotelWithDetails)
                .collect(Collectors.toList());
    }

    private HotelDTO enrichHotelWithDetails(HotelDTO hotel) {
        try {
            String token = authService.getAccessToken();
            HotelResponse details = amadeusClient.getHotelDetails("Bearer " + token, hotel.getHotelId());
            return mapToDetailedHotelDTO(hotel, details);
        } catch (FeignException.NotFound e) {
            log.warn("Hotel details not found for {}", hotel.getHotelId());
            return hotel;
        } catch (Exception e) {
            log.error("Error fetching details for hotel {}: {}", hotel.getHotelId(), e.getMessage());
            return hotel;
        }
    }


    private HotelDTO mapToDetailedHotelDTO(HotelDTO hotel, HotelResponse details) {
        if (details == null) return hotel;

        return hotel.toBuilder()
                .description(details.getDescription())
                .rating(details.getRating())
                .address(mapToAddress(details.getAddress()))
                .amenities(Optional.ofNullable(details.getAmenities()).orElse(Collections.emptyList()))
                .photos(mapMediaToPhotos(details.getImages()))
                .price(Optional.ofNullable(details.getPrice())
                        .map(p -> PriceDTO.builder()
                                .amount(p.getAmount())
                                .currency(p.getCurrency())
                                .build())
                        .orElse(hotel.getPrice()))
                .build();
    }

}
