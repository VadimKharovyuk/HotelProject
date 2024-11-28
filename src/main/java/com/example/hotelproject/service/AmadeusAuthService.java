package com.example.hotelproject.service;

import com.example.hotelproject.client.AmadeusProperties;
import com.example.hotelproject.dto.client.TokenResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class AmadeusAuthService {
    private final AmadeusProperties properties;
    private String accessToken;
    private LocalDateTime tokenExpiration;

    private static final String TOKEN_URL = "https://test.api.amadeus.com/v1/security/oauth2/token";

    @PostConstruct
    public void init() {
        refreshToken();
    }

    public String getAccessToken() {
        if (!isTokenValid()) {
            refreshToken();
        }
        return accessToken;
    }

    private synchronized void refreshToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", properties.getApiKey());
        body.add("client_secret", properties.getApiSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(TOKEN_URL, request, TokenResponse.class);
            if (response.getBody() != null) {
                this.accessToken = response.getBody().getAccessToken();
                this.tokenExpiration = LocalDateTime.now().plusSeconds(response.getBody().getExpiresIn());
                log.info("Amadeus token refreshed, expires: {}", tokenExpiration);
            }
        } catch (Exception e) {
            log.error("Failed to refresh Amadeus token", e);
            throw new RuntimeException("Failed to obtain Amadeus token", e);
        }
    }

    private boolean isTokenValid() {
        return accessToken != null && tokenExpiration != null &&
                LocalDateTime.now().plusMinutes(1).isBefore(tokenExpiration);
    }
}