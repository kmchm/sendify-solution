package com.sendify.server.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.sendify.server.dto.external.LandSttResponse;
import com.sendify.server.dto.internal.ShipmentDetailsDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbSchenkerClient {

    @Value("${dbschenker.tracking.url}")
    private String trackingUrl;

    private final ObjectMapper objectMapper;
    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        playwright = Playwright.create();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(true);
        browser = playwright.webkit().launch(launchOptions);
    }

    @PreDestroy
    public void cleanup() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    public ShipmentDetailsDto trackShipment(String oldReference) {
        try (Playwright playwright = Playwright.create()) {

            Page page = browser.newPage();
            page.route("**/*.{png,jpg,jpeg,svg,gif,css,woff,woff2}", route -> route.abort());

            Map<String, String> capturedResponses = new ConcurrentHashMap<>();

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

                                if (url.contains("LandStt:") && !capturedResponses.containsKey("land")) {
                                    System.out.println("Caught Land Status");
                                    capturedResponses.put("land", body);
                                }

                            } catch (Exception e) {
                                if (e.getMessage() == null || !e.getMessage().contains("Missing content of resource for given requestId")) {
                                    log.error("Error reading response body: {}", e.getMessage());
                                }
                            }

                            return capturedResponses.containsKey("land");
                        },
                        () -> page.navigate(trackingUrl + oldReference, new Page.NavigateOptions().setTimeout(10000))
                );
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            String landJson = capturedResponses.get("land");

            if (landJson == null) {
                throw new RuntimeException("Tracking reference " + oldReference + " not found. Please verify the ID.");
            }

            LandSttResponse landSttResponse = objectMapper.readValue(landJson, LandSttResponse.class);

            landSttResponse.getPackages().forEach(packageItem -> {
                log.info("Package ID: {}", packageItem.getId());
                packageItem.getEvents().forEach(packageEvent -> {
                    log.info("[{}] {} - {}",
                            packageEvent.getDate(),
                            packageEvent.getCode(),
                            packageEvent.getLocation());
                });
            });

            ShipmentDetailsDto.Party sender = ShipmentDetailsDto.Party.builder()
                    .name(String.valueOf(landSttResponse.getReferences().getShipper()))
                    .address(
                            ShipmentDetailsDto.Address.builder()
                                    .countryCode(landSttResponse.getLocation().getShipperPlace().getCountryCode())
                                    .country(landSttResponse.getLocation().getShipperPlace().getCountry())
                                    .city(landSttResponse.getLocation().getShipperPlace().getCity())
                                    .postCode(landSttResponse.getLocation().getShipperPlace().getPostCode())
                                    .build()
                    )
                    .build();


            ShipmentDetailsDto.Party receiver = ShipmentDetailsDto.Party.builder()
                    .name(String.valueOf(landSttResponse.getReferences().getConsignee()))
                    .address(
                            ShipmentDetailsDto.Address.builder()
                                    .countryCode(landSttResponse.getLocation().getConsigneePlace().getCountryCode())
                                    .country(landSttResponse.getLocation().getConsigneePlace().getCountry())
                                    .city(landSttResponse.getLocation().getConsigneePlace().getCity())
                                    .postCode(landSttResponse.getLocation().getConsigneePlace().getPostCode())
                                    .build()
                    )
                    .build();

            ShipmentDetailsDto.PackageDetails packageDetails = ShipmentDetailsDto.PackageDetails.builder()
                    .pieceCount(landSttResponse.getGoods().getPieces())
                    .weight(landSttResponse.getGoods().getWeight().getValue())
                    .weightUnit(landSttResponse.getGoods().getWeight().getUnit())
                    .dimensions(null)
                    .build();

            List<ShipmentDetailsDto.TrackingEvent> trackingHistory = landSttResponse.getEvents().stream()
                    .map(event -> ShipmentDetailsDto.TrackingEvent.builder()
                            .code(event.getCode())
                            .date(event.getDate())
                            .location(event.getLocation() != null ? event.getLocation().getName() : null)
                            .comment(event.getComment())
                            .build())
                    .toList();

            List<ShipmentDetailsDto.PackageTracking> packageTracking = landSttResponse.getPackages().stream()
                    .map(pkg -> ShipmentDetailsDto.PackageTracking.builder()
                            .packageId(pkg.getId())
                            .events(pkg.getEvents().stream()
                                    .map(event -> ShipmentDetailsDto.TrackingEvent.builder()
                                            .code(event.getCode())
                                            .date(event.getDate())
                                            .location(event.getLocation() != null ? event.getLocation() : null)
                                            .comment(event.getComment())
                                            .build())
                                    .toList())
                            .build())
                    .toList();

            return ShipmentDetailsDto.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .packageDetails(packageDetails)
                    .trackingHistory(trackingHistory)
                    .packageTracking(packageTracking)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching tracking info: " + e.getMessage(), e);
        }
    }
}
