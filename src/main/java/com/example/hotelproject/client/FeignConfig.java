package com.example.hotelproject.client;
import com.example.hotelproject.config.CustomErrorDecoder;
import feign.Client;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
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
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (requestTemplate.url().startsWith("/v1/v1/")) {
                String newUrl = requestTemplate.url().replaceFirst("/v1/v1/", "/v1/");
                requestTemplate.uri(newUrl);
            }
            if (requestTemplate.url().startsWith("/v2/v2/")) {
                String newUrl = requestTemplate.url().replaceFirst("/v2/v2/", "/v2/");
                requestTemplate.uri(newUrl);
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 2000, 3);
    }
}