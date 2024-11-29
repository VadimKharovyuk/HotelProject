package com.example.hotelproject.service;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.*;
import com.example.hotelproject.repository.AmadeusApiClient;
import com.example.hotelproject.service.AmadeusAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final AmadeusApiClient amadeusClient;
    private final AmadeusAuthService authService;

    public List<HotelDTO> searchHotels(HotelSearchRequest request) {
        String token = authService.getAccessToken();

        List<CityResponse> cities = amadeusClient.searchCities(
                "Bearer " + token,
                request.getDestination(),
                "CITY"
        );

        CityResponse city = cities.stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Город не найден"));

        HotelSearchResponse response = amadeusClient.searchHotels(
                "Bearer " + token,
                city.getIataCode(),
                20,
                request.getRoomCount(),
                request.getAdults(),
                request.getCheckIn().format(DateTimeFormatter.ISO_DATE),
                request.getCheckOut().format(DateTimeFormatter.ISO_DATE)
        );

        return mapToHotelDTOs(response);
    }


    private List<HotelDTO> mapToHotelDTOs(HotelSearchResponse response) {
        return response.getData().stream()
                .map(hotel -> HotelDTO.builder()
                        .hotelId(hotel.getHotelId())
                        .name(hotel.getName())
                        .description(hotel.getContact() != null ? hotel.getContact().getDescription() : null)
                        .rating(hotel.getStars())
                        .price(mapToPrice(hotel.getRate()))
                        .rooms(mapToRooms(hotel.getRooms()))
                        .amenities(hotel.getAmenities())
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


    private PriceDTO mapToPrice(Rate rate) {
        return rate != null ? PriceDTO.builder()
                .amount(rate.getAmount())
                .currency(rate.getCurrency())
                .build() : null;
    }


    private List<RoomDTO> mapToRooms(List<Room> rooms) {
        return rooms.stream()
                .map(room -> RoomDTO.builder()
                        .type(room.getType())
                        .price(PriceDTO.builder()
                                .amount(room.getRate().getAmount())
                                .currency(room.getRate().getCurrency())
                                .build())
                        .amenities(room.getAmenities())
                        .quantity(room.getAvailableCount())
                        .build())
                .collect(Collectors.toList());
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
}
