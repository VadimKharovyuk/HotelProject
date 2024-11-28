package com.example.hotelproject.controller;

import com.example.hotelproject.dto.auth.LoginDTO;
import com.example.hotelproject.dto.auth.UserCreateDTO;
import com.example.hotelproject.service.AuthenticationService;
import com.example.hotelproject.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(required = true) String email,
                        @RequestParam(required = true) String password,
                        Model model) {
        log.info("Received login request with email: '{}'", email);

        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email cannot be empty");
            return "auth/login";
        }

        try {

            authenticationService.authenticate(email, password);

            return "redirect:/user/dashboard";
        } catch (UsernameNotFoundException e) {
            log.error("Authentication failed - user not found: '{}'", email);
            model.addAttribute("error", "User not found");
            return "auth/login";
        } catch (BadCredentialsException e) {
            log.error("Authentication failed - invalid credentials for user: '{}'", email);
            model.addAttribute("error", "Invalid credentials");
            return "auth/login";
        }
    }



    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userCreateDTO", new UserCreateDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userCreateDTO") UserCreateDTO createDTO,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        userService.createUser(createDTO);
        return "redirect:/auth/login";
    }
}