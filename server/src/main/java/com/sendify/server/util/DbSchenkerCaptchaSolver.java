package com.sendify.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbSchenkerCaptchaSolver {

    private final ObjectMapper objectMapper;

    public String generateCaptcha(String captchaPuzzleBase64) {
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

    public String solvePuzzle(byte[] puzzleArray) {
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