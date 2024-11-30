package com.example.hotelproject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String inputName;

    @Column(nullable = false)
    private String iataCode;

    @Column(nullable = false)
    private String englishName;
}
