package com.example.hotelproject.client;
import feign.Client;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import feign.optionals.OptionalDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public Decoder feignDecoder() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new OptionalDecoder(new JacksonDecoder(objectMapper));
    }

    @Bean
    public feign.codec.Encoder feignEncoder() {
        return new SpringEncoder(() -> new HttpMessageConverters());
    }
    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> template.uri(template.url().replace("%2F", "/"));
    }

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }
}