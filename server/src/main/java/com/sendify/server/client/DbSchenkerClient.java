package com.sendify.server.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.sendify.server.dto.external.LandSttResponse;
import com.sendify.server.dto.external.ShipmentResponse;
import com.sendify.server.dto.external.TripResponse;
import com.sendify.server.dto.internal.ShipmentDetailsDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbSchenkerClient {

    @Value("${dbschenker.tracking.url}")
    private String trackingUrl;

    private final ObjectMapper objectMapper;

    public ShipmentDetailsDto trackShipment(String oldReference) {
        try (Playwright playwright = Playwright.create()) {

            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
            Browser browser = playwright.webkit().launch(launchOptions);
            Page page = browser.newPage();

            Map<String, String> capturedResponses = new ConcurrentHashMap<>();

            System.out.println("Navigating to: " + trackingUrl + oldReference);

            try {
                page.waitForResponse(
                        response -> {
                            String url = response.url();
                            int status = response.status();

                            if (status != 200) {
                                return false;
                            }

                            try {
                                String body = response.text();
                                if (body == null || body.trim().isEmpty()) {
                                    return false;
                                }

                                if (url.contains("shipments?query=") && !capturedResponses.containsKey("shipment")) {
                                    System.out.println("Caught Shipment Data");
                                    capturedResponses.put("shipment", body);
                                }

                                if (url.contains("LandStt:") && !capturedResponses.containsKey("land")) {
                                    System.out.println("Caught Land Status");
                                    capturedResponses.put("land", body);
                                }

                                if (url.contains("trip") && !capturedResponses.containsKey("trip")) {
                                    System.out.println("Caught Trip Status");
                                    capturedResponses.put("trip", body);
                                }
                            } catch (Exception e) {
                                if (e.getMessage() == null || !e.getMessage().contains("Missing content of resource for given requestId")) {
                                    log.error("Error reading response body: {}", e.getMessage());
                                }
                            }

                            return capturedResponses.containsKey("shipment")
                            && capturedResponses.containsKey("land") && capturedResponses.containsKey("trip");
                        },
                        () -> page.navigate(trackingUrl + oldReference)
                );
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            browser.close();

            String shipmentJson = capturedResponses.get("shipment");
            String landJson = capturedResponses.get("land");
            String tripJson = capturedResponses.get("trip");

            boolean allGood = shipmentJson != null || landJson != null || tripJson != null;

            if (!allGood) {
                throw new RuntimeException("Failed to capture shipment data for reference: " + oldReference);
            }

            ShipmentResponse shipmentResponse = objectMapper.readValue(shipmentJson, ShipmentResponse.class);
            LandSttResponse landSttResponse = objectMapper.readValue(landJson, LandSttResponse.class);
            TripResponse tripResponse = objectMapper.readValue(tripJson, TripResponse.class);

            log.info("SHIPMENT: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(shipmentResponse));
            log.info("LAND: {}" ,objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(landSttResponse));
            log.info("TRIP: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tripResponse));

            return null;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching tracking info: " + e.getMessage(), e);
        }
    }
}
