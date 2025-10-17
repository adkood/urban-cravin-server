package com.ashutosh.urban_cravin.helpers.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class PhonePeChecksum {

    public static String buildXVerify(String jsonPayload, String apiPath, String saltKey, String keyIndex) {
        try {
            String base64Payload = Base64.getEncoder().encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));
            String checksumInput = base64Payload + apiPath + saltKey;
            String sha256Hex = DigestUtils.sha256Hex(checksumInput);
            return sha256Hex + "###" + keyIndex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate X-VERIFY checksum", e);
        }
    }

    public static boolean verifyCallback(String payload, String xVerify, String saltKey, String keyIndex) {
        try {
            String expectedChecksum = buildXVerify(payload, "/pg/v1/webhook", saltKey, keyIndex);
            return expectedChecksum.equals(xVerify);
        } catch (Exception e) {
            return false;
        }
    }
}