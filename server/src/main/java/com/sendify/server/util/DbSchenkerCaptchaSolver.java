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

    /**
     * Decodes the captcha puzzle, solves each JWT-based puzzle, and returns the encoded solution.
     * The input is a base64-encoded string containing one or more JWTs, each with a puzzle payload.
     */
    public String generateCaptcha(String captchaPuzzleBase64) {
        try {
            // Decode the base64-encoded string to get the comma-separated JWTs
            String decoded = new String(Base64.getDecoder().decode(captchaPuzzleBase64));
            String[] jwtTokens = decoded.split(",");
            var solutions = new java.util.ArrayList<java.util.Map<String, String>>();

            for (String jwt : jwtTokens) {
                // JWT format: header.payload.signature (we only need the payload)
                String[] jwtParts = jwt.split("\\.");
                if (jwtParts.length < 2) {
                    throw new RuntimeException("Invalid JWT format");
                }
                String payloadB64 = jwtParts[1];
                // Decode the JWT payload (base64url)
                byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadB64);
                String payloadJson = new String(payloadBytes);
                com.fasterxml.jackson.databind.JsonNode payloadNode = objectMapper.readTree(payloadJson);
                // Extract the puzzle (base64-encoded) from the payload
                String puzzleB64 = payloadNode.get("puzzle").asText();
                byte[] puzzleBytes = Base64.getDecoder().decode(puzzleB64);
                // Solve the puzzle to get the nonce solution
                String solution = solvePuzzle(puzzleBytes);
                java.util.Map<String, String> entry = new java.util.HashMap<>();
                entry.put("jwt", jwt);
                entry.put("solution", solution);
                solutions.add(entry);
            }
            // Encode the solutions as JSON, then base64
            String json = objectMapper.writeValueAsString(solutions);
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate captcha", ex);
        }
    }

    /**
     * Solves a single captcha puzzle using a brute-force approach.
     * The puzzle is solved by finding a nonce such that the double SHA-256 hash
     * of (puzzle + nonce) is less than the target difficulty.
     *
     * The puzzleArray is 32 bytes. The difficulty is encoded in bytes 13 and 14:
     *   - t13 (byte 13) determines the exponent for the target calculation.
     *   - t14 (byte 14) is a multiplier for the target.
     * The target is calculated as: target = t14 * 2^(8 * (t13 - 3))
     * The solution is an 8-byte nonce, little-endian, such that:
     *   reverse(sha256(sha256(puzzle + nonce))) < target
     */
    public String solvePuzzle(byte[] puzzleArray) {
        // Extract difficulty parameters from the puzzle
        int t13 = Byte.toUnsignedInt(puzzleArray[13]);
        int t14 = Byte.toUnsignedInt(puzzleArray[14]);
        // Calculate the exponent for the target: r = 8 * (t13 - 3)
        int r = 8 * (t13 - 3);
        // o = 2^r
        java.math.BigInteger o = java.math.BigInteger.valueOf(2).pow(r);
        // targetDifficulty = t14 * o
        java.math.BigInteger targetDifficulty = java.math.BigInteger.valueOf(t14).multiply(o);

        int nonceValue = 0;
        byte[] foundSolution = null;
        // Brute-force search for a nonce that satisfies the hash condition
        while (nonceValue >= 0) {
            // Convert the integer nonce to an 8-byte little-endian array
            byte[] nonceArray = generateNonceArray(nonceValue);
            // Calculate the double SHA-256 hash and compare to the target
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
        // Return the solution as a base64-encoded string
        return Base64.getEncoder().encodeToString(foundSolution);
    }

    /**
     * Converts an integer nonce to an 8-byte array in little-endian order.
     * Each byte is filled with the least significant 8 bits of the number,
     * then the number is shifted right by 8 bits for the next byte.
     */
    private byte[] generateNonceArray(int numberValue) {
        byte[] n = new byte[8];
        for (int i = 0; i < n.length; i++) {
            int e = numberValue & 255;
            n[i] = (byte) e;
            numberValue = (numberValue - e) / 256;
        }
        return n;
    }

    /**
     * Calculates the double SHA-256 hash of the puzzle and nonce, reverses the result,
     * and returns it as a BigInteger for comparison with the target difficulty.
     *
     * Steps:
     *   1. Concatenate the 32-byte puzzle and 8-byte nonce into a 40-byte array.
     *   2. Compute SHA-256 hash of the 40-byte array.
     *   3. Compute SHA-256 hash of the previous hash (double SHA-256).
     *   4. Reverse the resulting 32-byte hash (to match endianness).
     *   5. Convert the reversed hash to a positive BigInteger.
     */
    private java.math.BigInteger calculateHashValue(byte[] nonceArray, byte[] puzzleArray) {
        try {
            // Concatenate puzzle and nonce
            byte[] combined = new byte[40];
            System.arraycopy(puzzleArray, 0, combined, 0, 32);
            System.arraycopy(nonceArray, 0, combined, 32, 8);

            // First SHA-256 hash
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] firstHash = sha256.digest(combined);
            // Second SHA-256 hash (double hash)
            byte[] secondHash = sha256.digest(firstHash);

            // Reverse the hash bytes (to match little-endian comparison)
            byte[] reversed = new byte[secondHash.length];
            for (int i = 0; i < secondHash.length; i++) {
                reversed[i] = secondHash[secondHash.length - 1 - i];
            }

            // Convert to BigInteger for comparison (positive value)
            return new java.math.BigInteger(1, reversed);
        } catch (Exception e) {
            log.error("Hash calculation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}