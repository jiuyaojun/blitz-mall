package com.blitz.seckill.application.service;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeckillPathService — HMAC path generation & validation")
class SeckillPathServiceTest {

    private static final String TEST_SECRET = "test-secret-2026";
    private SeckillPathService service;

    @BeforeEach
    void setUp() {
        service = new SeckillPathService(TEST_SECRET);
    }

    @Nested
    @DisplayName("generatePath()")
    class GeneratePath {

        @Test
        @DisplayName("returns non-null string with dot separator")
        void returnsNonNullWithDotSeparator() {
            String path = service.generatePath(1L, 100L);
            assertNotNull(path);
            String[] parts = path.split("\\.");
            assertEquals(2, parts.length, "Path should contain exactly one dot separator");
        }

        @Test
        @DisplayName("signature part is valid Base64URL without padding")
        void signatureIsValidBase64Url() {
            String path = service.generatePath(2L, 200L);
            String[] parts = path.split("\\.");
            // Should not throw when decoding
            byte[] decoded = Base64.getUrlDecoder().decode(parts[0]);
            assertTrue(decoded.length > 0);
            // Should not contain padding characters
            assertFalse(parts[0].contains("="));
        }

        @Test
        @DisplayName("expireTime part is numeric")
        void expireTimeIsNumeric() {
            String path = service.generatePath(3L, 300L);
            String[] parts = path.split("\\.");
            assertDoesNotThrow(() -> Long.parseLong(parts[1]));
        }
    }

    @Nested
    @DisplayName("validatePath()")
    class ValidatePath {

        @Test
        @DisplayName("returns correct spId when path is valid")
        void returnsSpIdWhenValid() {
            Long spId = 1L;
            Long userId = 100L;
            String path = service.generatePath(spId, userId);
            Long result = service.validatePath(path, spId, userId);
            assertEquals(spId, result);
        }

        @Test
        @DisplayName("returns null for tampered signature")
        void returnsNullForTamperedSignature() {
            Long spId = 1L;
            Long userId = 100L;
            String path = service.generatePath(spId, userId);
            String[] parts = path.split("\\.");
            // Tamper with the signature: flip first character
            String tamperedSig = (char) (parts[0].charAt(0) + 1) + parts[0].substring(1);
            String tamperedPath = tamperedSig + "." + parts[1];
            Long result = service.validatePath(tamperedPath, spId, userId);
            assertNull(result);
        }

        @Test
        @DisplayName("returns null for expired path")
        void returnsNullForExpiredPath() {
            // Manually craft path with an already-expired timestamp
            long oldExpireTime = 1L; // epoch + 1 second, definitely expired
            String payload = "1:100:" + oldExpireTime;
            HMac hMac = new HMac(HmacAlgorithm.HmacSHA256,
                TEST_SECRET.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(hMac.digest(payload));
            String expiredPath = signature + "." + oldExpireTime;

            Long result = service.validatePath(expiredPath, 1L, 100L);
            assertNull(result, "Expired path should return null");
        }

        @Test
        @DisplayName("returns null for malformed path — no dot")
        void returnsNullForMalformedPathNoDot() {
            Long result = service.validatePath("no-dot-path", 1L, 100L);
            assertNull(result);
        }

        @Test
        @DisplayName("returns null for malformed path — non-numeric expireTime")
        void returnsNullForNonNumericExpireTime() {
            Long result = service.validatePath("abc.notanumber", 1L, 100L);
            assertNull(result);
        }

        @Test
        @DisplayName("returns null when spId mismatch")
        void returnsNullForSpIdMismatch() {
            Long spId = 1L;
            Long userId = 100L;
            String path = service.generatePath(spId, userId);
            // Validate with a different spId
            Long result = service.validatePath(path, 2L, userId);
            assertNull(result);
        }

        @Test
        @DisplayName("returns null when userId mismatch")
        void returnsNullForUserIdMismatch() {
            Long spId = 1L;
            Long userId = 100L;
            String path = service.generatePath(spId, userId);
            // Validate with a different userId
            Long result = service.validatePath(path, spId, 200L);
            assertNull(result);
        }

        @Test
        @DisplayName("different secrets produce incompatible paths")
        void differentSecretsIncompatible() {
            SeckillPathService otherService = new SeckillPathService("different-secret");
            Long spId = 1L;
            Long userId = 100L;
            String path = otherService.generatePath(spId, userId);
            // Validate with our service (different secret)
            Long result = service.validatePath(path, spId, userId);
            assertNull(result);
        }
    }
}
