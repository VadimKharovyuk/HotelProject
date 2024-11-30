package com.example.hotelproject.service;

import com.example.hotelproject.model.CityMapping;
import com.example.hotelproject.repository.CityMappingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CityMappingService {
    private final CityMappingRepository cityMappingRepository;

    public Optional<CityMapping> findMapping(String cityName) {
        String normalizedName = cityName.toLowerCase().trim();
        log.debug("Searching for city mapping: {}", normalizedName);
        Optional<CityMapping> mapping = cityMappingRepository.findByInputNameIgnoreCase(normalizedName);
        mapping.ifPresent(m -> log.debug("Found mapping: {}", m));
        return mapping;
    }

    // Опционально: метод для получения всех доступных городов
    public List<CityMapping> getAllCities() {
        return cityMappingRepository.findAll();
    }

    // Опционально: метод для добавления нового города
    public CityMapping addCityMapping(CityMapping mapping) {
        return cityMappingRepository.save(mapping);
    }
}