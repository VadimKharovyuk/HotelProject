package com.example.hotelproject.service;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import com.example.hotelproject.Exception.UserAlreadyExistsException;
import com.example.hotelproject.config.SecurityUser;
import com.example.hotelproject.dto.auth.UserCreateDTO;
import com.example.hotelproject.dto.auth.UserDTO;
import com.example.hotelproject.enums.UserRole;
import com.example.hotelproject.maper.UserMapper;
import com.example.hotelproject.model.User;
import com.example.hotelproject.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Attempting to load user with email: '{}'", email);

        if (email == null || email.trim().isEmpty()) {
            log.error("Email is null or empty");
            throw new UsernameNotFoundException("Email cannot be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: '{}'", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.info("Successfully found user: {}", user.getEmail());
        return new SecurityUser(user);
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO createDTO) {
        validateNewUser(createDTO);

        User user = userMapper.toEntity(createDTO);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setRole(UserRole.ROLE_USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("Created user: {}", savedUser.getEmail());

        return userMapper.toDTO(savedUser);
    }


    private void validateNewUser(UserCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }


    public List<UserDTO> getAllUsers() {
        return userMapper.toDTOList(userRepository.findAll());
    }


    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userMapper.updateUserFromDTO(userDTO, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }



}