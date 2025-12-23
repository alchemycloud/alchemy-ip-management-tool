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
package cloud.alchemy.ip.api.mapper;

import cloud.alchemy.ip.api.dto.IpRecordDetailDto;
import cloud.alchemy.ip.api.dto.IpRecordDto;
import cloud.alchemy.ip.entity.IpAddressRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link IpRecordMapper}.
 */
@DisplayName("IpRecordMapper")
class IpRecordMapperTest {

    private IpRecordMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new IpRecordMapper();
    }

    @Nested
    @DisplayName("toDto")
    class ToDto {

        @Test
        @DisplayName("should return null when record is null")
        void shouldReturnNullWhenRecordIsNull() {
            IpRecordDto result = mapper.toDto(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            Instant now = Instant.now();
            IpAddressRecord record = createTestRecord(now);

            IpRecordDto result = mapper.toDto(record);

            assertThat(result).isNotNull();
            assertThat(result.ipAddress()).isEqualTo("192.168.1.100");
            assertThat(result.userId()).isEqualTo("user@example.com");
            assertThat(result.httpMethod()).isEqualTo("GET");
            assertThat(result.requestPath()).isEqualTo("/api/users");
            assertThat(result.tag()).isEqualTo("api-call");
            assertThat(result.countryCode()).isEqualTo("US");
            assertThat(result.createdAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should handle null optional fields")
        void shouldHandleNullOptionalFields() {
            IpAddressRecord record = IpAddressRecord.builder()
                    .ipAddress("192.168.1.100")
                    .createdAt(Instant.now())
                    .build();

            IpRecordDto result = mapper.toDto(record);

            assertThat(result).isNotNull();
            assertThat(result.ipAddress()).isEqualTo("192.168.1.100");
            assertThat(result.userId()).isNull();
            assertThat(result.httpMethod()).isNull();
            assertThat(result.requestPath()).isNull();
            assertThat(result.tag()).isNull();
            assertThat(result.countryCode()).isNull();
        }
    }

    @Nested
    @DisplayName("toDetailDto")
    class ToDetailDto {

        @Test
        @DisplayName("should return null when record is null")
        void shouldReturnNullWhenRecordIsNull() {
            IpRecordDetailDto result = mapper.toDetailDto(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            Instant now = Instant.now();
            IpAddressRecord record = createFullTestRecord(now);

            IpRecordDetailDto result = mapper.toDetailDto(record);

            assertThat(result).isNotNull();
            assertThat(result.ipAddress()).isEqualTo("192.168.1.100");
            assertThat(result.userId()).isEqualTo("user@example.com");
            assertThat(result.userAgent()).isEqualTo("Mozilla/5.0");
            assertThat(result.requestPath()).isEqualTo("/api/users");
            assertThat(result.httpMethod()).isEqualTo("GET");
            assertThat(result.tag()).isEqualTo("api-call");
            assertThat(result.countryCode()).isEqualTo("US");
            assertThat(result.city()).isEqualTo("New York");
            assertThat(result.region()).isEqualTo("NY");
            assertThat(result.latitude()).isEqualTo(40.7128);
            assertThat(result.longitude()).isEqualTo(-74.0060);
            assertThat(result.sourceHeader()).isEqualTo("X-Forwarded-For");
            assertThat(result.createdAt()).isEqualTo(now);
            assertThat(result.metadata()).isEqualTo("{\"key\":\"value\"}");
        }

        @Test
        @DisplayName("should handle null geo-location fields")
        void shouldHandleNullGeoLocationFields() {
            IpAddressRecord record = IpAddressRecord.builder()
                    .ipAddress("192.168.1.100")
                    .createdAt(Instant.now())
                    .build();

            IpRecordDetailDto result = mapper.toDetailDto(record);

            assertThat(result).isNotNull();
            assertThat(result.city()).isNull();
            assertThat(result.region()).isNull();
            assertThat(result.latitude()).isNull();
            assertThat(result.longitude()).isNull();
            assertThat(result.sourceHeader()).isNull();
            assertThat(result.metadata()).isNull();
        }
    }

    private IpAddressRecord createTestRecord(Instant timestamp) {
        return IpAddressRecord.builder()
                .ipAddress("192.168.1.100")
                .userId("user@example.com")
                .httpMethod("GET")
                .requestPath("/api/users")
                .tag("api-call")
                .countryCode("US")
                .createdAt(timestamp)
                .build();
    }

    private IpAddressRecord createFullTestRecord(Instant timestamp) {
        return IpAddressRecord.builder()
                .ipAddress("192.168.1.100")
                .userId("user@example.com")
                .httpMethod("GET")
                .requestPath("/api/users")
                .tag("api-call")
                .countryCode("US")
                .userAgent("Mozilla/5.0")
                .city("New York")
                .region("NY")
                .latitude(40.7128)
                .longitude(-74.0060)
                .sourceHeader("X-Forwarded-For")
                .metadata("{\"key\":\"value\"}")
                .createdAt(timestamp)
                .build();
    }
}
