package com.example.hotelproject.repository;

import com.example.hotelproject.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User>findByEmail(String email);

    boolean existsByEmail(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email);

}
