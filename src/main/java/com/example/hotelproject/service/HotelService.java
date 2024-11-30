package com.example.hotelproject.service;


import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.dto.client.*;
import com.example.hotelproject.model.CityMapping;
import com.example.hotelproject.repository.AmadeusApiClient;
import com.example.hotelproject.repository.AmadeusApiV1Client;
import com.example.hotelproject.repository.AmadeusApiV2Client;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {
//    private final AmadeusApiClient amadeusClient;
    private final AmadeusApiV1Client amadeusV1Client;
    private final AmadeusApiV2Client amadeusV2Client;
    private final AmadeusAuthService authService;
    private final CityMappingService cityMappingService;

    public List<HotelDTO> searchHotels(HotelSearchRequest request) {
        validateRequest(request);
        String token = authService.getAccessToken();
        String normalizedCityName = normalizeCityName(request.getDestination());

        try {
            CityMapping cityMapping = cityMappingService.findMapping(normalizedCityName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Город '%s' не найден. Попробуйте ввести название на английском языке.",
                                    request.getDestination())
                    ));

            HotelSearchResponse response = amadeusV1Client.searchHotels(
                    "Bearer " + token,
                    cityMapping.getIataCode(),
                    20,
                    "1,2,3,4,5",
                    "ALL"
            );

            if (response == null || response.getData() == null) {
                return Collections.emptyList();
            }

            List<HotelDTO> hotels = mapToHotelDTOs(response);
            enrichHotelsWithPrices(hotels, token, request);

            return hotels;
        } catch (Exception e) {
            log.error("Error searching for hotels: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("Ошибка при поиске отелей. Пожалуйста, попробуйте позже.");
        }
    }

    private List<HotelDTO> mapToHotelDTOs(HotelSearchResponse response) {
        return response.getData().stream()
                .map(hotel -> {
                    try {
                        return HotelDTO.builder()
                                .hotelId(hotel.getHotelId())
                                .name(hotel.getName())
                                .description(getDescription(hotel))
                                .amenities(getAmenities(hotel))
                                .address(mapAddress(hotel.getAddress()))
                                .photos(getPhotos(hotel))
                                .build();
                    } catch (Exception e) {
                        log.error("Error mapping hotel: {} - {}", hotel.getName(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(this::setDefaultValuesIfNeeded)
                .collect(Collectors.toList());
    }

    private void enrichHotelsWithPrices(List<HotelDTO> hotels, String token, HotelSearchRequest request) {
        List<List<HotelDTO>> batches = partition(hotels, 5);

        for (List<HotelDTO> batch : batches) {
            try {
                String hotelIds = batch.stream()
                        .map(HotelDTO::getHotelId)
                        .collect(Collectors.joining(","));

                HotelOffersResponse response = amadeusV2Client.getHotelOffers(
                        "Bearer " + token,
                        hotelIds,
                        request.getAdults(),
                        request.getCheckIn().toString(),
                        request.getCheckOut().toString(),
                        request.getRoomCount(),
                        "USD",
                        true
                );

                processBatchResponse(batch, response);
                Thread.sleep(200);
            } catch (Exception e) {
                log.error("Error fetching prices for batch: {}", e.getMessage());
                batch.forEach(hotel -> hotel.setPrice(createDefaultPrice()));
            }
        }
    }

    private void processBatchResponse(List<HotelDTO> batch, HotelOffersResponse offersResponse) {
        if (offersResponse != null && offersResponse.getData() != null) {
            Map<String, Price> priceMap = offersResponse.getData().stream()
                    .filter(offer -> offer.getOffers() != null && !offer.getOffers().isEmpty())
                    .collect(Collectors.toMap(
                            HotelOffer::getHotelId,
                            hotelOffer -> hotelOffer.getOffers().get(0).getPrice(),
                            (price1, price2) -> price1
                    ));

            batch.forEach(hotel -> {
                Price price = priceMap.get(hotel.getHotelId());
                if (price != null) {
                    hotel.setPrice(mapPrice(price));
                } else {
                    hotel.setPrice(createDefaultPrice());
                }
            });
        } else {
            batch.forEach(hotel -> hotel.setPrice(createDefaultPrice()));
        }
    }

    private PriceDTO mapPrice(Price price) {
        try {
            if (price == null) {
                return createDefaultPrice();
            }

            String priceValue = price.getTotal() != null && !price.getTotal().isEmpty() ?
                    price.getTotal() :
                    price.getBase() != null && !price.getBase().isEmpty() ?
                            price.getBase() : "0.00";

            priceValue = priceValue.replaceAll("[^\\d.]", "");

            return PriceDTO.builder()
                    .amount(new BigDecimal(priceValue))
                    .currency(price.getCurrency() != null ? price.getCurrency() : "USD")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping price: {}", e.getMessage());
            return createDefaultPrice();
        }
    }

    // Вспомогательные методы
    private void validateRequest(HotelSearchRequest request) {
        if (request.getRoomCount() < 1) {
            request.setRoomCount(1);
        }
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Название города не может быть пустым");
        }
    }

    private String normalizeCityName(String cityName) {
        return cityName.toLowerCase().trim();
    }

    private String getDescription(Hotel hotel) {
        return hotel.getDescription() != null ? hotel.getDescription() : "Описание отеля временно недоступно";
    }

    private List<String> getAmenities(Hotel hotel) {
        return hotel.getAmenities() == null || hotel.getAmenities().isEmpty() ?
                Arrays.asList("Wi-Fi", "Кондиционер", "Телевизор") :
                hotel.getAmenities();
    }

    private List<String> getPhotos(Hotel hotel) {
        return hotel.getPhotos() == null || hotel.getPhotos().isEmpty() ?
                Collections.singletonList("/images/default-hotel.jpg") :
                hotel.getPhotos();
    }

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

    private PriceDTO createDefaultPrice() {
        return PriceDTO.builder()
                .amount(new BigDecimal("0.00"))
                .currency("USD")
                .build();
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null || list.isEmpty() || size <= 0) {
            return Collections.emptyList();
        }

        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(new ArrayList<>(list.subList(i,
                    Math.min(i + size, list.size()))));
        }
        return partitions;
    }

    private HotelDTO setDefaultValuesIfNeeded(HotelDTO hotel) {
        if (hotel.getName() == null || hotel.getName().trim().isEmpty()) {
            hotel.setName("Название отеля уточняется");
        }
        if (hotel.getAddress() == null) {
            hotel.setAddress(createDefaultAddress());
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


