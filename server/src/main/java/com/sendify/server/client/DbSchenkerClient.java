package com.sendify.server.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendify.server.dto.external.LandSttResponse;
import com.sendify.server.dto.internal.ShipmentDetailsDto;
import com.sendify.server.mapper.LandSttResponseMapper;
import com.sendify.server.util.DbSchenkerCaptchaSolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbSchenkerClient {

    @Value("${dbschenker.tracking.api-base}")
    private String trackingApiBase;

    @Value("${dbschenker.tracking.max-retries}")
    private int maxRetries;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final LandSttResponseMapper landSttResponseMapper;
    private final DbSchenkerCaptchaSolver captchaSolver;

    public ShipmentDetailsDto trackShipment(String referenceNumber) {
        if (referenceNumber == null || referenceNumber.trim().isEmpty()) {
            throw new RuntimeException("Missing tracking reference");
        }

        try {
            String sttId = sttNumberQuery(referenceNumber, 0, null);
            return shipmentQuery(sttId, 0, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to track shipment: " + e.getMessage(), e);
        }
    }

    private String sttNumberQuery(String trackingNumber, int retryCount, String captcha) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (captcha != null) {
                headers.set("captcha-solution", captcha);
                log.info("Sending captcha-solution header: {}", captcha);
            }
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    trackingApiBase + "?query=" + trackingNumber,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            var node = objectMapper.readTree(response.getBody());
            return node.get("result").get(0).get("id").asText();
        } catch (HttpClientErrorException e) {
            String captchaSolution = handleCaptchaError(e, retryCount);
            return sttNumberQuery(trackingNumber, retryCount + 1, captchaSolution);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            throw new RuntimeException("Server error from DB Schenker: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query sttNumber: " + e.getMessage(), e);
        }
    }

    private ShipmentDetailsDto shipmentQuery(String sttId, int retryCount, String captcha) {
        log.debug("shipmentQuery called with sttId={}, retryCount={}, captcha={}", sttId, retryCount, captcha != null);
        try {
            HttpHeaders headers = new HttpHeaders();
            if (captcha != null) {
                headers.set("captcha-solution", captcha);
            }
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "application/json, text/plain, */*");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    trackingApiBase + "/land/" + sttId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String landJson = response.getBody();

            LandSttResponse landSttResponse = objectMapper.readValue(landJson, LandSttResponse.class);

            landSttResponse.getPackages().forEach(packageItem -> {
                log.info("Package ID: {}", packageItem.getId());
                packageItem.getEvents().forEach(packageEvent -> log.info("[{}] {} - {}",
                        packageEvent.getDate(),
                        packageEvent.getCode(),
                        packageEvent.getLocation()));
            });

            return landSttResponseMapper.map(landSttResponse);

        } catch (HttpClientErrorException e) {
            String captchaSolution = handleCaptchaError(e, retryCount);
            return shipmentQuery(sttId, retryCount + 1, captchaSolution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query shipment: " + e.getMessage(), e);
        }
    }

    private String handleCaptchaError(HttpClientErrorException e, int retries) {
        if (e.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS || retries >= maxRetries) {
            log.error("Captcha error or max retries reached: {}", e.getMessage());
            throw new RuntimeException("Failed to track shipment: " + e.getMessage(), e);
        }
        String captchaPuzzleBase64 = e.getResponseHeaders().getFirst("captcha-puzzle");
        if (captchaPuzzleBase64 == null) {
            throw new RuntimeException("Captcha required but puzzle not provided");
        }
        return captchaSolver.generateCaptcha(captchaPuzzleBase64);
    }
}
