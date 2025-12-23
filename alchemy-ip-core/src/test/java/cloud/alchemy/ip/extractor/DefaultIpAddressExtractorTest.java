/*
 * Copyright 2024 Alchemy Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.alchemy.ip.extractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultIpAddressExtractor}.
 */
@DisplayName("DefaultIpAddressExtractor")
class DefaultIpAddressExtractorTest {

    private DefaultIpAddressExtractor extractor;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        extractor = new DefaultIpAddressExtractor();
        request = new MockHttpServletRequest();
    }

    @Nested
    @DisplayName("extractIpAddress")
    class ExtractIpAddress {

        @Test
        @DisplayName("should return empty when request is null")
        void shouldReturnEmptyWhenRequestIsNull() {
            final Optional<String> result = extractor.extractIpAddress(null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should extract IP from CF-Connecting-IP header")
        void shouldExtractFromCfConnectingIp() {
            request.addHeader("CF-Connecting-IP", "203.0.113.195");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get());
        }

        @Test
        @DisplayName("should extract IP from True-Client-IP header")
        void shouldExtractFromTrueClientIp() {
            request.addHeader("True-Client-IP", "198.51.100.178");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("198.51.100.178", result.get());
        }

        @Test
        @DisplayName("should extract IP from X-Real-IP header")
        void shouldExtractFromXRealIp() {
            request.addHeader("X-Real-IP", "192.0.2.1");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("192.0.2.1", result.get());
        }

        @Test
        @DisplayName("should extract first IP from X-Forwarded-For header")
        void shouldExtractFirstIpFromXForwardedFor() {
            request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get());
        }

        @Test
        @DisplayName("should handle X-Forwarded-For with spaces")
        void shouldHandleXForwardedForWithSpaces() {
            request.addHeader("X-Forwarded-For", "  203.0.113.195  ,  70.41.3.18  ");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get());
        }

        @Test
        @DisplayName("should skip private IPs in X-Forwarded-For when public IP available")
        void shouldSkipPrivateIpsInXForwardedFor() {
            request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1, 203.0.113.195");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get());
        }

        @Test
        @DisplayName("should extract IP from Forwarded header")
        void shouldExtractFromForwardedHeader() {
            request.addHeader("Forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("192.0.2.60", result.get());
        }

        @Test
        @DisplayName("should handle Forwarded header with quoted value")
        void shouldHandleForwardedWithQuotedValue() {
            request.addHeader("Forwarded", "for=\"192.0.2.60\";proto=http");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("192.0.2.60", result.get());
        }

        @Test
        @DisplayName("should fall back to remote address when no headers present")
        void shouldFallBackToRemoteAddress() {
            request.setRemoteAddr("198.51.100.1");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("198.51.100.1", result.get());
        }

        @Test
        @DisplayName("should prefer CF-Connecting-IP over X-Forwarded-For")
        void shouldPreferCfConnectingIpOverXForwardedFor() {
            request.addHeader("CF-Connecting-IP", "203.0.113.195");
            request.addHeader("X-Forwarded-For", "198.51.100.178");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get());
        }

        @Test
        @DisplayName("should ignore 'unknown' header values")
        void shouldIgnoreUnknownValues() {
            request.addHeader("X-Real-IP", "unknown");
            request.setRemoteAddr("192.0.2.1");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("192.0.2.1", result.get());
        }

        @Test
        @DisplayName("should handle IPv6 addresses")
        void shouldHandleIpv6Addresses() {
            request.addHeader("X-Real-IP", "2001:0db8:85a3:0000:0000:8a2e:0370:7334");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", result.get());
        }

        @Test
        @DisplayName("should normalize IPv6 loopback")
        void shouldNormalizeIpv6Loopback() {
            request.addHeader("X-Real-IP", "0:0:0:0:0:0:0:1");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("::1", result.get());
        }

        @Test
        @DisplayName("should extract from Azure header")
        void shouldExtractFromAzureHeader() {
            request.addHeader("X-Azure-ClientIP", "20.30.40.50");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("20.30.40.50", result.get());
        }

        @Test
        @DisplayName("should extract from Fastly header")
        void shouldExtractFromFastlyHeader() {
            request.addHeader("Fastly-Client-IP", "151.101.1.57");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("151.101.1.57", result.get());
        }

        @Test
        @DisplayName("should handle invalid IP address format")
        void shouldHandleInvalidIpFormat() {
            request.addHeader("X-Real-IP", "not-an-ip");
            request.setRemoteAddr("192.0.2.1");
            final Optional<String> result = extractor.extractIpAddress(request);
            assertTrue(result.isPresent());
            assertEquals("192.0.2.1", result.get());
        }
    }

    @Nested
    @DisplayName("constructor and configuration")
    class ConstructorAndConfiguration {

        @Test
        @DisplayName("should create extractor with default settings")
        void shouldCreateWithDefaultSettings() {
            final DefaultIpAddressExtractor defaultExtractor = new DefaultIpAddressExtractor();
            assertTrue(defaultExtractor.isTrustAllProxies());
            assertTrue(defaultExtractor.getTrustedProxies().isEmpty());
        }

        @Test
        @DisplayName("should create extractor with custom settings")
        void shouldCreateWithCustomSettings() {
            final List<String> trustedProxies = List.of("10.0.0.1", "10.0.0.2");
            final DefaultIpAddressExtractor customExtractor = new DefaultIpAddressExtractor(false, trustedProxies);

            assertFalse(customExtractor.isTrustAllProxies());
            assertEquals(2, customExtractor.getTrustedProxies().size());
            assertTrue(customExtractor.getTrustedProxies().contains("10.0.0.1"));
        }

        @Test
        @DisplayName("should handle null trusted proxies list")
        void shouldHandleNullTrustedProxies() {
            final DefaultIpAddressExtractor customExtractor = new DefaultIpAddressExtractor(true, null);
            assertNotNull(customExtractor.getTrustedProxies());
            assertTrue(customExtractor.getTrustedProxies().isEmpty());
        }

        @Test
        @DisplayName("should return immutable trusted proxies list")
        void shouldReturnImmutableTrustedProxiesList() {
            final List<String> trustedProxies = List.of("10.0.0.1");
            final DefaultIpAddressExtractor customExtractor = new DefaultIpAddressExtractor(false, trustedProxies);

            assertThrows(UnsupportedOperationException.class, () ->
                    customExtractor.getTrustedProxies().add("10.0.0.2")
            );
        }
    }
}
