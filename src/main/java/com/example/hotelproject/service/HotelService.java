package com.example.hotelproject.service;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.*;
import com.example.hotelproject.repository.AmadeusApiClient;
import com.example.hotelproject.service.AmadeusAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {
    private final AmadeusApiClient amadeusClient;
    private final AmadeusAuthService authService;

    public List<HotelDTO> searchHotels(HotelSearchRequest request) {

        if (request.getRoomCount() < 1) {
            log.warn("Invalid room count: {}. Using default value of 1", request.getRoomCount());
        }
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


//
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class HotelService {
//    private final AmadeusApiClient amadeusClient;
//    private final AmadeusAuthService authService;
//
//    private static final int MAX_RETRY_ATTEMPTS = 3;
//    private static final long INITIAL_RETRY_DELAY_MS = 1000;
//    private static final int RATE_LIMIT_DELAY_MS = 2000;
//    private static final int MAX_PARALLEL_REQUESTS = 5;
//    private static final int SERVER_ERROR_DELAY_MS = 1500;
//    private static final int DEFAULT_RADIUS = 20;
//    private static final String DEFAULT_RATINGS = "1,2,3,4,5";
//    private static final String DEFAULT_HOTEL_SOURCE = "ALL";
//
//    private final Semaphore requestSemaphore = new Semaphore(MAX_PARALLEL_REQUESTS);
//
//    public List<HotelDTO> searchHotels(HotelSearchRequest request) {
//        String token = authService.getAccessToken();
//        log.debug("Starting hotel search for destination: {}", request.getDestination());
//
//        // Find the city
//        CityData city = findCity(token, request.getDestination());
//        log.info("Found city with IATA code: {}", city.getIataCode());
//
//        // Search for hotels
//        List<HotelDTO> hotels = searchHotelsInCity(token, city.getIataCode());
//
//        // Enrich with details if hotels found
//        if (!hotels.isEmpty()) {
//            hotels = enrichHotelsWithDetails(hotels);
//            log.info("Found and enriched {} hotels in {}", hotels.size(), city.getName());
//        } else {
//            log.warn("No hotels found in {}", city.getName());
//        }
//
//        return hotels;
//    }
//
//    private CityData findCity(String token, String cityName) {
//        try {
//            LocationResponse locationResponse = amadeusClient.searchCities(
//                    "Bearer " + token,
//                    cityName,
//                    "CITY"
//            );
//
//            if (locationResponse == null || locationResponse.getData() == null) {
//                throw new ResourceNotFoundException("Не удалось найти информацию о городе: " + cityName);
//            }
//
//            return locationResponse.getData().stream()
//                    .filter(c -> c != null && "CITY".equals(c.getSubType()))
//                    .findFirst()
//                    .orElseThrow(() -> new ResourceNotFoundException("Город не найден: " + cityName));
//        } catch (FeignException e) {
//            log.error("Error searching for city {}: {}", cityName, e.getMessage());
//            throw new ResourceNotFoundException("Ошибка при поиске города: " + cityName);
//        }
//    }
//
//    private List<HotelDTO> searchHotelsInCity(String token, String cityCode) {
//        try {
//            HotelSearchResponse response = amadeusClient.searchHotels(
//                    "Bearer " + token,
//                    cityCode,
//                    DEFAULT_RADIUS,
//                    DEFAULT_RATINGS,
//                    DEFAULT_HOTEL_SOURCE
//            );
//
//            if (response == null || response.getData() == null) {
//                return Collections.emptyList();
//            }
//
//            return mapToHotelDTOs(response);
//        } catch (FeignException e) {
//            log.error("Error searching hotels in city {}: {}", cityCode, e.getMessage());
//            return Collections.emptyList();
//        }
//    }
//
//    private List<HotelDTO> mapToHotelDTOs(HotelSearchResponse response) {
//        return Optional.ofNullable(response.getData())
//                .orElse(Collections.emptyList())
//                .stream()
//                .filter(Objects::nonNull)
//                .map(hotel -> HotelDTO.builder()
//                        .hotelId(hotel.getHotelId())
//                        .name(hotel.getName())
//                        .description(Optional.ofNullable(hotel.getContact())
//                                .map(Contact::getDescription)
//                                .orElse(null))
//                        .rating(hotel.getStars())
//                        .price(mapToPrice(hotel.getRate()))
//                        .rooms(mapToRooms(hotel.getRooms()))
//                        .amenities(Optional.ofNullable(hotel.getAmenities())
//                                .orElse(Collections.emptyList()))
//                        .address(mapToAddress(hotel.getAddress()))
//                        .photos(mapMediaToPhotos(hotel.getMediaUrls()))
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    public List<HotelDTO> enrichHotelsWithDetails(List<HotelDTO> hotels) {
//        String token = authService.getAccessToken();
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger errorCount = new AtomicInteger(0);
//
//        // Use regular stream instead of parallelStream for better control
//        return hotels.stream()
//                .map(hotel -> {
//                    try {
//                        requestSemaphore.acquire();
//                        try {
//                            HotelDTO enrichedHotel = enrichHotelWithDetails(hotel, token);
//                            if (!hotel.equals(enrichedHotel)) {
//                                successCount.incrementAndGet();
//                            } else {
//                                errorCount.incrementAndGet();
//                            }
//                            return enrichedHotel;
//                        } finally {
//                            requestSemaphore.release();
//                            Thread.sleep(100); // Small delay between requests
//                        }
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                        log.error("Прерван процесс обогащения данных отеля {}", hotel.getHotelId());
//                        return hotel;
//                    }
//                })
//                .collect(Collectors.toList());
//    }
//
//    private HotelDTO enrichHotelWithDetails(HotelDTO hotel, String token) {
//        int attempts = 0;
//        long currentDelay = INITIAL_RETRY_DELAY_MS;
//
//        while (attempts < MAX_RETRY_ATTEMPTS) {
//            try {
//                if (attempts > 0) {
//                    Thread.sleep(currentDelay);
//                }
//
//                HotelResponse details = amadeusClient.getHotelDetails("Bearer " + token, hotel.getHotelId());
//                if (details != null) {
//                    return mapToDetailedHotelDTO(hotel, details);
//                }
//                return hotel;
//
//            } catch (FeignException.TooManyRequests e) {
//                attempts++;
//                currentDelay = RATE_LIMIT_DELAY_MS * (long) Math.pow(2, attempts - 1);
//                handleRetryableError(hotel, attempts, "превышение лимита запросов", currentDelay, e);
//
//            } catch (FeignException.InternalServerError e) {
//                attempts++;
//                currentDelay = SERVER_ERROR_DELAY_MS * (long) Math.pow(2, attempts - 1);
//
//                if (isAmadeusInternalError(e)) {
//                    log.error("Внутренняя ошибка Amadeus для отеля {}: {}",
//                            hotel.getHotelId(), extractErrorDetails(e));
//
//                    if (attempts >= MAX_RETRY_ATTEMPTS) {
//                        return hotel;
//                    }
//
//                    log.warn("Попытка {} из {}. Ожидание {} мс перед повторным запросом для отеля {}",
//                            attempts, MAX_RETRY_ATTEMPTS, currentDelay, hotel.getHotelId());
//                }
//
//            } catch (FeignException.NotFound e) {
//                log.warn("Детали отеля не найдены для {}", hotel.getHotelId());
//                return hotel;
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.error("Прерван процесс получения деталей отеля {}", hotel.getHotelId());
//                return hotel;
//
//            } catch (Exception e) {
//                log.error("Непредвиденная ошибка при получении деталей отеля {}: {}",
//                        hotel.getHotelId(), e.getMessage());
//                return hotel;
//            }
//        }
//
//        log.error("Исчерпаны все попытки получения деталей для отеля {}", hotel.getHotelId());
//        return hotel;
//    }
//
//    private void handleRetryableError(HotelDTO hotel, int attempt, String errorType, long delay, Exception e) {
//        if (attempt >= MAX_RETRY_ATTEMPTS) {
//            log.error("Превышено максимальное количество попыток для отеля {} ({})",
//                    hotel.getHotelId(), errorType);
//        } else {
//            log.warn("Попытка {}/{}. {}, ожидание {} мс для отеля {}",
//                    attempt, MAX_RETRY_ATTEMPTS, errorType, delay, hotel.getHotelId());
//        }
//    }
//
//    private boolean isAmadeusInternalError(FeignException.InternalServerError e) {
//        try {
//            String responseBody = e.contentUTF8();
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(responseBody);
//            JsonNode errors = root.get("errors");
//
//            if (errors != null && errors.isArray() && errors.size() > 0) {
//                JsonNode firstError = errors.get(0);
//                return firstError.has("code") && firstError.get("code").asInt() == 38189;
//            }
//        } catch (Exception ex) {
//            log.warn("Ошибка при парсинге ответа об ошибке: {}", ex.getMessage());
//        }
//        return false;
//    }
//
//    private String extractErrorDetails(FeignException e) {
//        try {
//            String responseBody = e.contentUTF8();
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(responseBody);
//            JsonNode errors = root.get("errors");
//
//            if (errors != null && errors.isArray() && errors.size() > 0) {
//                JsonNode firstError = errors.get(0);
//                return String.format("Code: %s, Title: %s, Detail: %s",
//                        firstError.get("code").asText(),
//                        firstError.get("title").asText(),
//                        firstError.get("detail").asText());
//            }
//        } catch (Exception ex) {
//            log.warn("Ошибка при извлечении деталей ошибки: {}", ex.getMessage());
//        }
//        return e.getMessage();
//    }
//
//    private List<String> mapMediaToPhotos(List<Media> mediaList) {
//        return Optional.ofNullable(mediaList)
//                .map(list -> list.stream()
//                        .map(Media::getUri)
//                        .collect(Collectors.toList()))
//                .orElse(Collections.emptyList());
//    }
//
//    private List<RoomDTO> mapToRooms(List<Room> rooms) {
//        if (rooms == null) return Collections.emptyList();
//
//        return rooms.stream()
//                .filter(room -> room != null && room.getRate() != null)
//                .map(room -> RoomDTO.builder()
//                        .type(room.getType())
//                        .price(mapToPrice(room.getRate()))
//                        .amenities(Optional.ofNullable(room.getAmenities()).orElse(Collections.emptyList()))
//                        .quantity(room.getAvailableCount())
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    private PriceDTO mapToPrice(Rate rate) {
//        if (rate == null) return null;
//        return PriceDTO.builder()
//                .amount(rate.getAmount())
//                .currency(rate.getCurrency())
//                .build();
//    }
//
//    private AddressDTO mapToAddress(Address address) {
//        return AddressDTO.builder()
//                .street(address.getStreet())
//                .cityName(address.getCity())
//                .country(address.getCountry())
//                .latitude(address.getLatitude())
//                .longitude(address.getLongitude())
//                .coordinates(address.getLatitude() + "," + address.getLongitude())
//                .build();
//    }
//
//    private HotelDTO mapToDetailedHotelDTO(HotelDTO hotel, HotelResponse details) {
//        if (details == null) return hotel;
//
//        return hotel.toBuilder()
//                .description(details.getDescription())
//                .rating(details.getRating())
//                .address(mapToAddress(details.getAddress()))
//                .amenities(Optional.ofNullable(details.getAmenities()).orElse(Collections.emptyList()))
//                .photos(mapMediaToPhotos(details.getImages()))
//                .price(Optional.ofNullable(details.getPrice())
//                        .map(p -> PriceDTO.builder()
//                                .amount(p.getAmount())
//                                .currency(p.getCurrency())
//                                .build())
//                        .orElse(hotel.getPrice()))
//                .build();
//    }
//}
