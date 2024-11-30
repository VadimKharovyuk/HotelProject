package com.example.hotelproject.service;


import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.*;
import com.example.hotelproject.model.CityMapping;
import com.example.hotelproject.repository.AmadeusApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {
    private final AmadeusApiClient amadeusClient;
    private final AmadeusAuthService authService;
    private final CityMappingService cityMappingService;

    public List<HotelDTO> searchHotels(HotelSearchRequest request) {
        validateRequest(request);
        String token = authService.getAccessToken();
        String normalizedCityName = normalizeCityName(request.getDestination());
        log.debug("Searching hotels for city: {}", normalizedCityName);


        try {
            // Получаем маппинг города
            CityMapping cityMapping = cityMappingService.findMapping(normalizedCityName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Город '%s' не найден. Попробуйте ввести название на английском языке.",
                                    request.getDestination())
                    ));

            log.debug("Found city mapping: {} -> {} (IATA: {})",
                    normalizedCityName, cityMapping.getEnglishName(), cityMapping.getIataCode());

            // Используем IATA код для поиска отелей
            HotelSearchResponse response = amadeusClient.searchHotels(
                    "Bearer " + token,
                    cityMapping.getIataCode(),
                    20,
                    "1,2,3,4,5",
                    "ALL"
            );
            log.debug("API Response: {}", response);

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }

            List<HotelDTO> hotels = mapToHotelDTOs(response);
            log.info("Found {} hotels in {}", hotels.size(), cityMapping.getEnglishName());
            return hotels;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching for hotels in city: {}", request.getDestination(), e);
            throw new ResourceNotFoundException(
                    String.format("Ошибка при поиске отелей в городе '%s'. Пожалуйста, попробуйте позже.",
                            request.getDestination())
            );
        }
    }

    private void validateRequest(HotelSearchRequest request) {
        if (request.getRoomCount() < 1) {
            log.warn("Invalid room count: {}. Using default value of 1", request.getRoomCount());
            request.setRoomCount(1);
        }
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Название города не может быть пустым");
        }
    }

    private String normalizeCityName(String cityName) {
        return cityName.toLowerCase().trim();
    }


    private List<HotelDTO> mapToHotelDTOs(HotelSearchResponse response) {
        return response.getData().stream()
                .map(hotelData -> {
                    log.debug("Mapping hotel data: {}", hotelData);
                    try {
                        return HotelDTO.builder()
                                .hotelId(hotelData.getHotelId()) // исправлено с getId на getHotelId
                                .name(getValidName(hotelData))
                                .description(getDescription(hotelData))
                                .amenities(getAmenities(hotelData))
                                .address(mapAddress(hotelData.getAddress()))
                                .price(mapPrice(hotelData.getPrice()))
                                .photos(getPhotos(hotelData))
                                .rooms(mapRooms(hotelData.getRooms()))
                                .build();
                    } catch (Exception e) {
                        log.error("Error mapping hotel data: {}", hotelData, e);
                        return createDefaultHotelDTO(hotelData.getName(), hotelData.getHotelId());
                    }
                })
                .collect(Collectors.toList());
    }

    private String getValidName(Hotel hotel) {
        String name = hotel.getName();
        return (name != null && !name.trim().isEmpty()) ? name : "Название отеля уточняется";
    }

    private String getDescription(Hotel hotel) {
        return hotel.getDescription() != null ? hotel.getDescription() : "Описание отеля временно недоступно";
    }

    private List<String> getAmenities(Hotel hotel) {
        if (hotel.getAmenities() == null || hotel.getAmenities().isEmpty()) {
            return Arrays.asList("Wi-Fi", "Кондиционер", "Телевизор");
        }
        return hotel.getAmenities();
    }

    private List<String> getPhotos(Hotel hotel) {
        if (hotel.getPhotos() == null || hotel.getPhotos().isEmpty()) {
            return Collections.singletonList("/images/default-hotel.jpg");
        }
        return hotel.getPhotos();
    }

    private PriceDTO mapPrice(Price price) {
        try {
            if (price == null) {
                return createDefaultPrice();
            }
            return PriceDTO.builder()
                    .amount(price.getAmount())
                    .currency(price.getCurrency() != null ? price.getCurrency() : "USD")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping price: {}", price, e);
            return createDefaultPrice();
        }
    }

    private PriceDTO createDefaultPrice() {
        return PriceDTO.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();
    }

    private HotelDTO createDefaultHotelDTO(String name, String hotelId) {
        return HotelDTO.builder()
                .hotelId(hotelId)
                .name(name != null ? name : "Название недоступно")
                .description("Описание временно недоступно")
                .price(createDefaultPrice())
                .amenities(Arrays.asList("Wi-Fi", "Кондиционер"))
                .address(createDefaultAddress())
                .photos(Collections.singletonList("/images/default-hotel.jpg"))
                .rooms(Collections.singletonList(createDefaultRoom()))
                .build();
    }








    // Обновленный метод mapAddress для более надежной обработки
    private AddressDTO mapAddress(Address address) {
        if (address == null) {
            return createDefaultAddress();
        }
        return AddressDTO.builder()
                .street(getValidString(address.getStreet(), "Адрес уточняется"))
                .city(getValidString(address.getCity(), "Киев"))
                .country(getValidString(address.getCountry(), "Украина"))
                .postalCode(getValidString(address.getPostalCode(), ""))
                .build();
    }

    private String getValidString(String value, String defaultValue) {
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    private AddressDTO createDefaultAddress() {
        return AddressDTO.builder()
                .city("Киев")
                .street("Адрес уточняется")
                .country("Украина")
                .postalCode("")
                .build();
    }

    // Обновленный метод для более надежного маппинга комнат
    private List<RoomDTO> mapRooms(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return Collections.singletonList(createDefaultRoom());
        }
        return rooms.stream()
                .map(this::mapRoom)
                .collect(Collectors.toList());
    }

    private RoomDTO mapRoom(Room room) {
        try {
            return RoomDTO.builder()
                    .type(getValidString(room.getType(), "Стандартный номер"))
                    .description(getValidString(room.getDescription(), "Описание номера уточняется"))
                    .price(mapPrice(room.getPrice()))
                    .capacity(room.getCapacity() != null ? room.getCapacity() : 2)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping room: {}", room, e);
            return createDefaultRoom();
        }
    }

    private RoomDTO createDefaultRoom() {
        return RoomDTO.builder()
                .type("Стандартный номер")
                .description("Описание номера уточняется")
                .price(createDefaultPrice())
                .capacity(2)
                .build();
    }




    private boolean isEmptyResponse(LocationResponse response) {
        return response == null || response.getData() == null || response.getData().isEmpty();
    }



    private CityData findCity(String token, String normalizedCityName, String originalDestination) {
        // Пробуем найти город через маппинг
        String searchQuery = cityMappingService.findMapping(normalizedCityName)
                .map(CityMapping::getEnglishName)
                .orElse(originalDestination);

        LocationResponse locationResponse = amadeusClient.searchCities(
                "Bearer " + token,
                searchQuery,
                "CITY"
        );

        // Если не нашли через маппинг, пробуем прямой поиск
        if (locationResponse == null || locationResponse.getData() == null || locationResponse.getData().isEmpty()) {
            locationResponse = amadeusClient.searchCities(
                    "Bearer " + token,
                    originalDestination,
                    "CITY"
            );
        }

        if (locationResponse == null || locationResponse.getData() == null || locationResponse.getData().isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("Город '%s' не найден. Попробуйте ввести название на английском языке.",
                            originalDestination)
            );
        }

        return findBestMatchingCity(locationResponse.getData());
    }

    private HotelSearchResponse searchHotelsInCity(String token, String cityCode) {
        return amadeusClient.searchHotels(
                "Bearer " + token,
                cityCode,
                20,
                "1,2,3,4,5",
                "ALL"
        );
    }

    private CityData findBestMatchingCity(List<CityData> cities) {
        return cities.stream()
                .filter(city -> "CITY".equals(city.getSubType()))
                .findFirst()
                .orElse(null);
    }




    private HotelDTO setDefaultValuesIfNeeded(HotelDTO hotel) {
        if (hotel.getName() == null || hotel.getName().trim().isEmpty()) {
            hotel.setName("Название отеля уточняется");
        }
        if (hotel.getPrice() == null) {
            hotel.setPrice(PriceDTO.builder()
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .build());
        }
        if (hotel.getAddress() == null) {
            hotel.setAddress(AddressDTO.builder()
                    .city("Киев")
                    .street("Адрес уточняется")
                    .country("Украина")
                    .build());
        }
        if (hotel.getAmenities() == null) {
            hotel.setAmenities(Arrays.asList("Wi-Fi", "Кондиционер", "Телевизор"));
        }
        if (hotel.getPhotos() == null || hotel.getPhotos().isEmpty()) {
            hotel.setPhotos(Collections.singletonList("/images/default-hotel.jpg"));
        }
        return hotel;
    }


}