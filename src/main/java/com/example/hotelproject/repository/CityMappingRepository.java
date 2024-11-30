package com.example.hotelproject.repository;

import com.example.hotelproject.model.CityMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityMappingRepository extends JpaRepository<CityMapping, Long> {
    Optional<CityMapping> findByInputNameIgnoreCase(String inputName);
}
