package com.example.hotelproject.controller;

import com.example.hotelproject.Exception.UserAlreadyExistsException;
import com.example.hotelproject.dto.auth.LoginDTO;
import com.example.hotelproject.dto.auth.UserCreateDTO;
import com.example.hotelproject.service.AuthenticationService;
import com.example.hotelproject.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.AuthenticationException;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;


    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("login", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("login") LoginDTO loginDTO,
                        BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/login";
        }

        try {
            authenticationService.authenticate(loginDTO.getEmail(), loginDTO.getPassword());
            return "redirect:/dashboard";
        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/auth/login";
        }
    }


    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userCreateDTO", new UserCreateDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userCreateDTO") UserCreateDTO createDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.createUser(createDTO);
            redirectAttributes.addFlashAttribute("success", "Registration successful!");
            return "redirect:/auth/login";
        } catch (UserAlreadyExistsException e) {
            result.rejectValue("email", "email.exists", "Email already registered");
            return "auth/register";
        }
    }
}