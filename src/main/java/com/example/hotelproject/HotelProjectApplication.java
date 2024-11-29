package com.example.hotelproject;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.hotelproject.repository")
public class HotelProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelProjectApplication.class, args);
    }
//	Обработка ошибок аутентификации:
//	•	Вместо использования BadCredentialsException в AuthenticationService
//	можно бросать более специфичные исключения, чтобы клиенту возвращались более полезные сообщения
//	(например, пользователь заблокирован, аккаунт не подтверждён и т.д.).
}
