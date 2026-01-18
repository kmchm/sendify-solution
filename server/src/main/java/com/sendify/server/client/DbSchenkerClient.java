package com.sendify.server.client;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

import com.sendify.server.dto.external.LandSttResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendify.server.dto.internal.ShipmentDetailsDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DbSchenkerClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    private static final String TRACKING_API_BASE = "https://www.dbschenker.com/nges-portal/api/public/tracking-public/shipments";
    private static final int MAX_RETRIES = 10;

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
                    TRACKING_API_BASE + "?query=" + trackingNumber,
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
                    TRACKING_API_BASE + "/land/" + sttId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            String landJson = response.getBody();

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

        } catch (HttpClientErrorException e) {
            String captchaSolution = handleCaptchaError(e, retryCount);
            return shipmentQuery(sttId, retryCount + 1, captchaSolution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to query shipment: " + e.getMessage(), e);
        }
    }

    private String handleCaptchaError(HttpClientErrorException e, int retries) {
        if (e.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS || retries >= MAX_RETRIES) {
            log.error("Captcha error or max retries reached: {}", e.getMessage());
            throw new RuntimeException("Failed to track shipment: " + e.getMessage(), e);
        }
        String captchaPuzzleBase64 = e.getResponseHeaders().getFirst("captcha-puzzle");
        if (captchaPuzzleBase64 == null) {
            throw new RuntimeException("Captcha required but puzzle not provided");
        }
        return generateCaptcha(captchaPuzzleBase64);
    }

    private String generateCaptcha(String captchaPuzzleBase64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(captchaPuzzleBase64));
            String[] jwtTokens = decoded.split(",");
            var solutions = new java.util.ArrayList<java.util.Map<String, String>>();

            for (String jwt : jwtTokens) {
                String[] jwtParts = jwt.split("\\.");
                if (jwtParts.length < 2) {
                    throw new RuntimeException("Invalid JWT format");
                }
                String payloadB64 = jwtParts[1];
                byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadB64);
                String payloadJson = new String(payloadBytes);
                com.fasterxml.jackson.databind.JsonNode payloadNode = objectMapper.readTree(payloadJson);
                String puzzleB64 = payloadNode.get("puzzle").asText();
                byte[] puzzleBytes = Base64.getDecoder().decode(puzzleB64);
                String solution = solvePuzzle(puzzleBytes);
                java.util.Map<String, String> entry = new java.util.HashMap<>();
                entry.put("jwt", jwt);
                entry.put("solution", solution);
                solutions.add(entry);
            }
            String json = objectMapper.writeValueAsString(solutions);
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate captcha", ex);
        }
    }

    private String solvePuzzle(byte[] puzzleArray) {
        int t13 = Byte.toUnsignedInt(puzzleArray[13]);
        int t14 = Byte.toUnsignedInt(puzzleArray[14]);
        int r = 8 * (t13 - 3);
        java.math.BigInteger o = java.math.BigInteger.valueOf(2).pow(r);
        java.math.BigInteger targetDifficulty = java.math.BigInteger.valueOf(t14).multiply(o);

        int nonceValue = 0;
        byte[] foundSolution = null;
        while (nonceValue >= 0) {
            byte[] nonceArray = generateNonceArray(nonceValue);
            java.math.BigInteger hashResult = calculateHashValue(nonceArray, puzzleArray);
            if (hashResult.compareTo(targetDifficulty) < 0) {
                foundSolution = nonceArray;
                break;
            }
            nonceValue++;
        }
        if (foundSolution == null) {
            log.error("No captcha solution found");
            return "";
        }
        return Base64.getEncoder().encodeToString(foundSolution);
    }

    private byte[] generateNonceArray(int numberValue) {
        byte[] n = new byte[8];
        for (int i = 0; i < n.length; i++) {
            int e = numberValue & 255;
            n[i] = (byte) e;
            numberValue = (numberValue - e) / 256;
        }
        return n;
    }

    private java.math.BigInteger calculateHashValue(byte[] nonceArray, byte[] puzzleArray) {
        try {
            byte[] combined = new byte[40];
            System.arraycopy(puzzleArray, 0, combined, 0, 32);
            System.arraycopy(nonceArray, 0, combined, 32, 8);

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] firstHash = sha256.digest(combined);
            byte[] secondHash = sha256.digest(firstHash);

            byte[] reversed = new byte[secondHash.length];
            for (int i = 0; i < secondHash.length; i++) {
                reversed[i] = secondHash[secondHash.length - 1 - i];
            }

            return new java.math.BigInteger(1, reversed);
        } catch (Exception e) {
            log.error("Hash calculation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}
