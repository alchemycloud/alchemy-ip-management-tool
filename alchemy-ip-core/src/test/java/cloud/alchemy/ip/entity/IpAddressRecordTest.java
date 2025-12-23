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
package cloud.alchemy.ip.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link IpAddressRecord}.
 */
@DisplayName("IpAddressRecord")
class IpAddressRecordTest {

    @Test
    @DisplayName("should use builder to create record with all fields")
    void shouldUseBuilderToCreateRecordWithAllFields() {
        final Instant now = Instant.now();

        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress("203.0.113.195")
                .userId("user456")
                .userAgent("Mozilla/5.0")
                .requestPath("/api/users")
                .httpMethod("GET")
                .tag("api-access")
                .countryCode("US")
                .city("New York")
                .region("NY")
                .latitude(40.7128)
                .longitude(-74.0060)
                .createdAt(now)
                .metadata("{\"key\":\"value\"}")
                .sourceHeader("X-Real-IP")
                .build();

        assertEquals("203.0.113.195", record.getIpAddress());
        assertEquals("user456", record.getUserId());
        assertEquals("Mozilla/5.0", record.getUserAgent());
        assertEquals("/api/users", record.getRequestPath());
        assertEquals("GET", record.getHttpMethod());
        assertEquals("api-access", record.getTag());
        assertEquals("US", record.getCountryCode());
        assertEquals("New York", record.getCity());
        assertEquals("NY", record.getRegion());
        assertEquals(40.7128, record.getLatitude());
        assertEquals(-74.0060, record.getLongitude());
        assertEquals(now, record.getCreatedAt());
        assertEquals("{\"key\":\"value\"}", record.getMetadata());
        assertEquals("X-Real-IP", record.getSourceHeader());
    }

    @Test
    @DisplayName("should create record with minimal required fields")
    void shouldCreateRecordWithMinimalFields() {
        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress("192.168.1.1")
                .build();

        assertEquals("192.168.1.1", record.getIpAddress());
        assertNull(record.getUserId());
        assertNull(record.getUserAgent());
    }

    @Test
    @DisplayName("should throw exception when ipAddress is null")
    void shouldThrowExceptionWhenIpAddressIsNull() {
        final IpAddressRecord.Builder builder = IpAddressRecord.builder()
                .userId("user123");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("should throw exception when ipAddress is blank")
    void shouldThrowExceptionWhenIpAddressIsBlank() {
        final IpAddressRecord.Builder builder = IpAddressRecord.builder()
                .ipAddress("   ");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("should create copy with toBuilder")
    void shouldCreateCopyWithToBuilder() {
        final IpAddressRecord original = IpAddressRecord.builder()
                .ipAddress("192.168.1.1")
                .userId("user123")
                .httpMethod("GET")
                .build();

        final IpAddressRecord copy = original.toBuilder()
                .httpMethod("POST")
                .tag("modified")
                .build();

        assertEquals("192.168.1.1", copy.getIpAddress());
        assertEquals("user123", copy.getUserId());
        assertEquals("POST", copy.getHttpMethod());
        assertEquals("modified", copy.getTag());

        // Original should be unchanged
        assertEquals("GET", original.getHttpMethod());
        assertNull(original.getTag());
    }

    @Test
    @DisplayName("should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress("192.168.1.1")
                .userId("user123")
                .requestPath("/api/test")
                .httpMethod("POST")
                .build();

        final String str = record.toString();

        assertTrue(str.contains("192.168.1.1"));
        assertTrue(str.contains("user123"));
        assertTrue(str.contains("/api/test"));
        assertTrue(str.contains("POST"));
    }

    @Test
    @DisplayName("should implement equals based on id")
    void shouldImplementEqualsBasedOnId() {
        final IpAddressRecord record1 = IpAddressRecord.builder()
                .ipAddress("192.168.1.1")
                .build();

        final IpAddressRecord record2 = IpAddressRecord.builder()
                .ipAddress("192.168.1.2")
                .build();

        // Both have null IDs, so they should be equal based on id
        assertEquals(record1, record2);
    }

    @Test
    @DisplayName("should implement hashCode consistently")
    void shouldImplementHashCodeConsistently() {
        final IpAddressRecord record1 = IpAddressRecord.builder()
                .ipAddress("192.168.1.1")
                .build();

        final IpAddressRecord record2 = IpAddressRecord.builder()
                .ipAddress("192.168.1.2")
                .build();

        assertEquals(record1.hashCode(), record2.hashCode());
    }

    @Test
    @DisplayName("should create record with IPv6 address")
    void shouldCreateRecordWithIpv6Address() {
        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                .build();

        assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", record.getIpAddress());
    }

    @Test
    @DisplayName("should preserve all geo-location fields")
    void shouldPreserveGeoLocationFields() {
        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress("192.168.1.1")
                .countryCode("CA")
                .city("Toronto")
                .region("Ontario")
                .latitude(43.6532)
                .longitude(-79.3832)
                .build();

        assertEquals("CA", record.getCountryCode());
        assertEquals("Toronto", record.getCity());
        assertEquals("Ontario", record.getRegion());
        assertEquals(43.6532, record.getLatitude());
        assertEquals(-79.3832, record.getLongitude());
    }

    @Test
    @DisplayName("should handle null optional fields gracefully")
    void shouldHandleNullOptionalFieldsGracefully() {
        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress("10.0.0.1")
                .userId(null)
                .userAgent(null)
                .build();

        assertEquals("10.0.0.1", record.getIpAddress());
        assertNull(record.getUserId());
        assertNull(record.getUserAgent());
        assertNull(record.getRequestPath());
        assertNull(record.getHttpMethod());
    }
}
