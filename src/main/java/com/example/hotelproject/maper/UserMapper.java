package com.example.hotelproject.maper;

import com.example.hotelproject.dto.auth.UserCreateDTO;
import com.example.hotelproject.dto.auth.UserDTO;
import com.example.hotelproject.enums.UserRole;
import com.example.hotelproject.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .enabled(user.isEnabled())
                .build();
    }

    public List<UserDTO> toDTOList(List<User> users) {
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public User toEntity(UserCreateDTO createDTO) {
        return User.builder()
                .email(createDTO.getEmail())
                .password(createDTO.getPassword()) // Пароль будет зашифрован в сервисе
                .firstName(createDTO.getFirstName())
                .lastName(createDTO.getLastName())
                .role(UserRole.ROLE_USER)
                .enabled(true)
                .build();
    }

    public void updateUserFromDTO(UserDTO userDTO, User user) {
        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        // Не обновляем пароль через DTO
        // Не обновляем роль через обычное DTO
    }
}
