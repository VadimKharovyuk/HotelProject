package com.example.hotelproject.controller;

import com.example.hotelproject.config.SecurityUser;
import com.example.hotelproject.dto.auth.UserDTO;
import com.example.hotelproject.service.CurrentUserService;
import com.example.hotelproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Получение текущего пользователя через сервис
        Long userId = currentUserService.getCurrentUserId();

        // Использование UserService для получения информации о пользователе
        UserDTO userDTO = userService.getUserById(userId);
        model.addAttribute("user", userDTO);

        return "user/dashboard";
    }
}