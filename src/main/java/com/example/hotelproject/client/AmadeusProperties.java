package com.example.hotelproject.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "amadeus.api")
@Data
public class AmadeusProperties {
    private String apiKey = "iw4rvJB7AGq93gbA4BdmrXRjEPNr8For";
    private String apiSecret = "owdrvnSGsZ04VMaK";
}
