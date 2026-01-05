package com.sendify.server.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.sendify.server.dto.SchenkerResponse;
import com.sendify.server.dto.ShipmentDetails;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class DbSchenkerClient {

    @Value("${dbschenker.tracking.url}")
    private String trackingUrl;

    private final ObjectMapper objectMapper;

    public ShipmentDetails trackShipment(String reference) {
        try (Playwright playwright = Playwright.create()) {

            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
            Browser browser = playwright.webkit().launch(launchOptions);
            Page page = browser.newPage();

            var responsePromise = page.waitForResponse(
                    response -> response.url().contains("shipments?query=" + reference) && response.status() == 200,
                    () -> {
                        page.navigate(trackingUrl + reference);
                    }
            );

            String jsonBody = responsePromise.text();
            SchenkerResponse response = objectMapper.readValue(jsonBody, SchenkerResponse.class);

            browser.close();

            if (response.getResult() == null || response.getResult().isEmpty()) {
                throw new RuntimeException("Shipment not found.");
            }

            return response.getResult().getFirst();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching tracking info: " + e.getMessage());
        }
    }
}