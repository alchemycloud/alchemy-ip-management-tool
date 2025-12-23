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
package cloud.alchemy.ip.api.controller;

import cloud.alchemy.ip.api.mapper.IpRecordMapper;
import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.repository.IpAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link IpStatsController}.
 */
@WebMvcTest(IpStatsController.class)
@Import(IpStatsController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("IpStatsController")
class IpStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IpAddressRepository repository;

    @MockBean
    private IpRecordMapper mapper;

    private List<IpAddressRecord> testRecords;
    private Instant testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = Instant.now();

        IpAddressRecord record1 = createTestRecord("192.168.1.100", "user1@example.com", "US", "New York");
        IpAddressRecord record2 = createTestRecord("192.168.1.101", "user2@example.com", "US", "Los Angeles");
        IpAddressRecord record3 = createTestRecord("192.168.1.100", "user1@example.com", "UK", "London");

        testRecords = List.of(record1, record2, record3);

        // Default mapper behavior
        when(mapper.toDto(any(IpAddressRecord.class))).thenAnswer(invocation -> {
            IpAddressRecord record = invocation.getArgument(0);
            return new cloud.alchemy.ip.api.dto.IpRecordDto(
                    record.getId(),
                    record.getIpAddress(),
                    record.getUserId(),
                    record.getHttpMethod(),
                    record.getRequestPath(),
                    record.getTag(),
                    record.getCountryCode(),
                    record.getCreatedAt()
            );
        });
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/summary")
    class GetSummaryStats {

        @Test
        @DisplayName("should return summary statistics")
        void shouldReturnSummaryStatistics() throws Exception {
            when(repository.count()).thenReturn(3L);
            when(repository.findAll()).thenReturn(testRecords);
            when(repository.findByCreatedAtBetween(any(Instant.class), any(Instant.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(testRecords, PageRequest.of(0, 1), 3));
            List<Object[]> frequentIpsList = new java.util.ArrayList<>();
            frequentIpsList.add(new Object[]{"192.168.1.100", 2L});
            Page<Object[]> frequentIpsPage = new PageImpl<>(frequentIpsList);
            when(repository.findFrequentIpAddresses(anyLong(), any(Pageable.class)))
                    .thenReturn(frequentIpsPage);
            when(repository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(testRecords, PageRequest.of(0, 10), 3));

            mockMvc.perform(get("/api/alchemy-ip/stats/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecords", is(3)))
                    .andExpect(jsonPath("$.uniqueIpAddresses", is(2)))
                    .andExpect(jsonPath("$.uniqueUsers", is(2)))
                    .andExpect(jsonPath("$.topCountries", hasKey("US")))
                    .andExpect(jsonPath("$.topIpAddresses", hasSize(greaterThanOrEqualTo(0))));
        }

        @Test
        @DisplayName("should handle empty database")
        void shouldHandleEmptyDatabase() throws Exception {
            when(repository.count()).thenReturn(0L);
            when(repository.findAll()).thenReturn(List.of());
            when(repository.findByCreatedAtBetween(any(Instant.class), any(Instant.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));
            when(repository.findFrequentIpAddresses(anyLong(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(new java.util.ArrayList<Object[]>()));
            when(repository.findAll(any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            mockMvc.perform(get("/api/alchemy-ip/stats/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRecords", is(0)))
                    .andExpect(jsonPath("$.uniqueIpAddresses", is(0)))
                    .andExpect(jsonPath("$.uniqueUsers", is(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/timeline")
    class GetTimelineData {

        @Test
        @DisplayName("should return timeline data with default 30 days")
        void shouldReturnTimelineDataWithDefault30Days() throws Exception {
            when(repository.findByCreatedAtBetween(any(Instant.class), any(Instant.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

            mockMvc.perform(get("/api/alchemy-ip/stats/timeline"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.labels", hasSize(31))) // 30 days + today
                    .andExpect(jsonPath("$.counts", hasSize(31)));
        }

        @Test
        @DisplayName("should respect days parameter")
        void shouldRespectDaysParameter() throws Exception {
            when(repository.findByCreatedAtBetween(any(Instant.class), any(Instant.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

            mockMvc.perform(get("/api/alchemy-ip/stats/timeline")
                            .param("days", "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.labels", hasSize(8))) // 7 days + today
                    .andExpect(jsonPath("$.counts", hasSize(8)));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/geographic")
    class GetGeoDistribution {

        @Test
        @DisplayName("should return geographic distribution")
        void shouldReturnGeoDistribution() throws Exception {
            when(repository.findAll()).thenReturn(testRecords);

            mockMvc.perform(get("/api/alchemy-ip/stats/geographic"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.countryDistribution", hasKey("US")))
                    .andExpect(jsonPath("$.countryDistribution", hasKey("UK")))
                    .andExpect(jsonPath("$.cityDistribution", hasKey("New York")))
                    .andExpect(jsonPath("$.cityDistribution", hasKey("Los Angeles")))
                    .andExpect(jsonPath("$.cityDistribution", hasKey("London")));
        }

        @Test
        @DisplayName("should handle records without geo data")
        void shouldHandleRecordsWithoutGeoData() throws Exception {
            IpAddressRecord recordWithoutGeo = IpAddressRecord.builder()
                    .ipAddress("192.168.1.100")
                    .createdAt(testTimestamp)
                    .build();

            when(repository.findAll()).thenReturn(List.of(recordWithoutGeo));

            mockMvc.perform(get("/api/alchemy-ip/stats/geographic"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.countryDistribution", anEmptyMap()))
                    .andExpect(jsonPath("$.cityDistribution", anEmptyMap()));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/frequent")
    class GetFrequentIps {

        @Test
        @DisplayName("should return frequent IP addresses")
        void shouldReturnFrequentIpAddresses() throws Exception {
            List<Object[]> frequentIpList = new java.util.ArrayList<>();
            frequentIpList.add(new Object[]{"192.168.1.100", 5L});
            frequentIpList.add(new Object[]{"192.168.1.101", 3L});
            Page<Object[]> frequentIps = new PageImpl<>(frequentIpList, PageRequest.of(0, 20), 2);

            when(repository.findFrequentIpAddresses(eq(2L), any(Pageable.class))).thenReturn(frequentIps);

            mockMvc.perform(get("/api/alchemy-ip/stats/frequent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].ipAddress", is("192.168.1.100")))
                    .andExpect(jsonPath("$.content[0].count", is(5)))
                    .andExpect(jsonPath("$.content[1].ipAddress", is("192.168.1.101")))
                    .andExpect(jsonPath("$.content[1].count", is(3)));
        }

        @Test
        @DisplayName("should respect threshold parameter")
        void shouldRespectThresholdParameter() throws Exception {
            Page<Object[]> frequentIps = new PageImpl<>(new java.util.ArrayList<>(), PageRequest.of(0, 20), 0);

            when(repository.findFrequentIpAddresses(eq(10L), any(Pageable.class))).thenReturn(frequentIps);

            mockMvc.perform(get("/api/alchemy-ip/stats/frequent")
                            .param("threshold", "10"))
                    .andExpect(status().isOk());

            verify(repository).findFrequentIpAddresses(eq(10L), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/ip/{ipAddress}/count")
    class GetCountByIp {

        @Test
        @DisplayName("should return count for IP address")
        void shouldReturnCountForIpAddress() throws Exception {
            when(repository.countByIpAddress("192.168.1.100")).thenReturn(5L);

            mockMvc.perform(get("/api/alchemy-ip/stats/ip/192.168.1.100/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(5)));

            verify(repository).countByIpAddress("192.168.1.100");
        }

        @Test
        @DisplayName("should return zero for unknown IP")
        void shouldReturnZeroForUnknownIp() throws Exception {
            when(repository.countByIpAddress("10.0.0.1")).thenReturn(0L);

            mockMvc.perform(get("/api/alchemy-ip/stats/ip/10.0.0.1/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/user/{userId}/count")
    class GetCountByUser {

        @Test
        @DisplayName("should return count for user ID")
        void shouldReturnCountForUserId() throws Exception {
            when(repository.countByUserId("user@example.com")).thenReturn(10L);

            mockMvc.perform(get("/api/alchemy-ip/stats/user/user@example.com/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(10)));

            verify(repository).countByUserId("user@example.com");
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/user/{userId}/distinct-ips")
    class GetDistinctIpsForUser {

        @Test
        @DisplayName("should return distinct IPs for user")
        void shouldReturnDistinctIpsForUser() throws Exception {
            when(repository.countDistinctIpAddressesByUserId("user@example.com")).thenReturn(3L);
            when(repository.findDistinctIpAddressesByUserId("user@example.com"))
                    .thenReturn(List.of("192.168.1.100", "192.168.1.101", "192.168.1.102"));

            mockMvc.perform(get("/api/alchemy-ip/stats/user/user@example.com/distinct-ips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(3)))
                    .andExpect(jsonPath("$.ipAddresses", hasSize(3)))
                    .andExpect(jsonPath("$.ipAddresses[0]", is("192.168.1.100")));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/stats/ip/{ipAddress}/distinct-users")
    class GetDistinctUsersForIp {

        @Test
        @DisplayName("should return distinct user count for IP")
        void shouldReturnDistinctUserCountForIp() throws Exception {
            when(repository.countDistinctUsersByIpAddress("192.168.1.100")).thenReturn(5L);

            mockMvc.perform(get("/api/alchemy-ip/stats/ip/192.168.1.100/distinct-users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count", is(5)));

            verify(repository).countDistinctUsersByIpAddress("192.168.1.100");
        }
    }

    private IpAddressRecord createTestRecord(String ipAddress, String userId, String countryCode, String city) {
        return IpAddressRecord.builder()
                .ipAddress(ipAddress)
                .userId(userId)
                .countryCode(countryCode)
                .city(city)
                .httpMethod("GET")
                .requestPath("/api/test")
                .createdAt(testTimestamp)
                .build();
    }
}
