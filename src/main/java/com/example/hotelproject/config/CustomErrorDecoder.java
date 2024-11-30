package com.example.hotelproject.config;

import com.example.hotelproject.Exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // Читаем тело ответа
            String responseBody = IOUtils.toString(response.body().asReader(StandardCharsets.UTF_8));
            log.error("API Error Response: {}", responseBody);

            if (response.status() == 404) {
                return new ResourceNotFoundException("Ресурс не найден в API Amadeus");
            }

            return defaultErrorDecoder.decode(methodKey, response);
        } catch (IOException e) {
            return new Exception("Error decoding response");
        }
    }
}