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

import cloud.alchemy.ip.api.dto.SearchCriteriaDto;
import cloud.alchemy.ip.api.exception.ApiExceptionHandler;
import cloud.alchemy.ip.api.mapper.IpRecordMapper;
import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.repository.IpAddressRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link IpRecordController}.
 */
@WebMvcTest(IpRecordController.class)
@Import({IpRecordController.class, ApiExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("IpRecordController")
class IpRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IpAddressRepository repository;

    @MockBean
    private IpRecordMapper mapper;

    private IpAddressRecord testRecord;
    private Instant testTimestamp;

    @BeforeEach
    void setUp() {
        testTimestamp = Instant.now();
        testRecord = createTestRecord("192.168.1.100", "user@example.com", testTimestamp);

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

        when(mapper.toDetailDto(any(IpAddressRecord.class))).thenAnswer(invocation -> {
            IpAddressRecord record = invocation.getArgument(0);
            return new cloud.alchemy.ip.api.dto.IpRecordDetailDto(
                    record.getId(),
                    record.getIpAddress(),
                    record.getUserId(),
                    record.getUserAgent(),
                    record.getRequestPath(),
                    record.getHttpMethod(),
                    record.getTag(),
                    record.getCountryCode(),
                    record.getCity(),
                    record.getRegion(),
                    record.getLatitude(),
                    record.getLongitude(),
                    record.getSourceHeader(),
                    record.getCreatedAt(),
                    record.getMetadata()
            );
        });
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records")
    class GetAllRecords {

        @Test
        @DisplayName("should return paginated records")
        void shouldReturnPaginatedRecords() throws Exception {
            List<IpAddressRecord> records = List.of(testRecord);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].ipAddress", is("192.168.1.100")))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.pageNumber", is(0)))
                    .andExpect(jsonPath("$.pageSize", is(20)));

            verify(repository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("should respect pagination parameters")
        void shouldRespectPaginationParameters() throws Exception {
            Page<IpAddressRecord> page = new PageImpl<>(List.of(), PageRequest.of(2, 10), 0);

            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records")
                            .param("page", "2")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageNumber", is(2)))
                    .andExpect(jsonPath("$.pageSize", is(10)));
        }

        @Test
        @DisplayName("should limit page size to 100")
        void shouldLimitPageSizeTo100() throws Exception {
            Page<IpAddressRecord> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);

            when(repository.findAll(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records")
                            .param("size", "500"))
                    .andExpect(status().isOk());

            verify(repository).findAll(argThat((Pageable pageable) -> pageable.getPageSize() <= 100));
        }

        @Test
        @DisplayName("should return empty page when no records")
        void shouldReturnEmptyPageWhenNoRecords() throws Exception {
            Page<IpAddressRecord> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            when(repository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/api/alchemy-ip/records"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records/{id}")
    class GetRecordById {

        @Test
        @DisplayName("should return record when found")
        void shouldReturnRecordWhenFound() throws Exception {
            when(repository.findById(1L)).thenReturn(Optional.of(testRecord));

            mockMvc.perform(get("/api/alchemy-ip/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ipAddress", is("192.168.1.100")))
                    .andExpect(jsonPath("$.userId", is("user@example.com")));

            verify(repository).findById(1L);
        }

        @Test
        @DisplayName("should return 404 when record not found")
        void shouldReturn404WhenRecordNotFound() throws Exception {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/alchemy-ip/records/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records/ip/{ipAddress}")
    class GetRecordsByIp {

        @Test
        @DisplayName("should return records for IP address")
        void shouldReturnRecordsForIpAddress() throws Exception {
            List<IpAddressRecord> records = List.of(testRecord);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findByIpAddress(eq("192.168.1.100"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records/ip/192.168.1.100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].ipAddress", is("192.168.1.100")));

            verify(repository).findByIpAddress(eq("192.168.1.100"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records/user/{userId}")
    class GetRecordsByUser {

        @Test
        @DisplayName("should return records for user ID")
        void shouldReturnRecordsForUserId() throws Exception {
            List<IpAddressRecord> records = List.of(testRecord);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findByUserId(eq("user@example.com"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records/user/user@example.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].userId", is("user@example.com")));

            verify(repository).findByUserId(eq("user@example.com"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records/tag/{tag}")
    class GetRecordsByTag {

        @Test
        @DisplayName("should return records for tag")
        void shouldReturnRecordsForTag() throws Exception {
            IpAddressRecord recordWithTag = createTestRecordWithTag("login");
            List<IpAddressRecord> records = List.of(recordWithTag);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findByTag(eq("login"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records/tag/login"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));

            verify(repository).findByTag(eq("login"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records/country/{countryCode}")
    class GetRecordsByCountry {

        @Test
        @DisplayName("should return records for country code")
        void shouldReturnRecordsForCountryCode() throws Exception {
            IpAddressRecord recordWithCountry = createTestRecordWithCountry("US");
            List<IpAddressRecord> records = List.of(recordWithCountry);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findByCountryCode(eq("US"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records/country/us"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));

            verify(repository).findByCountryCode(eq("US"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/alchemy-ip/records/date-range")
    class GetRecordsByDateRange {

        @Test
        @DisplayName("should return records within date range")
        void shouldReturnRecordsWithinDateRange() throws Exception {
            Instant startDate = testTimestamp.minus(1, ChronoUnit.DAYS);
            Instant endDate = testTimestamp.plus(1, ChronoUnit.DAYS);
            List<IpAddressRecord> records = List.of(testRecord);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findByCreatedAtBetween(any(Instant.class), any(Instant.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/alchemy-ip/records/date-range")
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));

            verify(repository).findByCreatedAtBetween(any(Instant.class), any(Instant.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("POST /api/alchemy-ip/records/search")
    class SearchRecords {

        @Test
        @DisplayName("should search with criteria")
        void shouldSearchWithCriteria() throws Exception {
            SearchCriteriaDto criteria = new SearchCriteriaDto(
                    "192.168.1.100",
                    "user@example.com",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<IpAddressRecord> records = List.of(testRecord);
            Page<IpAddressRecord> page = new PageImpl<>(records, PageRequest.of(0, 20), 1);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(post("/api/alchemy-ip/records/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));

            verify(repository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("should search with empty criteria")
        void shouldSearchWithEmptyCriteria() throws Exception {
            SearchCriteriaDto criteria = new SearchCriteriaDto(
                    null, null, null, null, null, null, null, null
            );

            Page<IpAddressRecord> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(post("/api/alchemy-ip/records/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/alchemy-ip/records/before/{timestamp}")
    class DeleteOldRecords {

        @Test
        @DisplayName("should delete records before timestamp")
        void shouldDeleteRecordsBeforeTimestamp() throws Exception {
            Instant cutoffDate = testTimestamp.minus(30, ChronoUnit.DAYS);

            when(repository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(5L);

            mockMvc.perform(delete("/api/alchemy-ip/records/before/" + cutoffDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deletedCount", is(5)));

            verify(repository).deleteByCreatedAtBefore(any(Instant.class));
        }

        @Test
        @DisplayName("should return zero when no records deleted")
        void shouldReturnZeroWhenNoRecordsDeleted() throws Exception {
            Instant cutoffDate = Instant.parse("2020-01-01T00:00:00Z");

            when(repository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(0L);

            mockMvc.perform(delete("/api/alchemy-ip/records/before/" + cutoffDate.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deletedCount", is(0)));
        }
    }

    private IpAddressRecord createTestRecord(String ipAddress, String userId, Instant createdAt) {
        return IpAddressRecord.builder()
                .ipAddress(ipAddress)
                .userId(userId)
                .httpMethod("GET")
                .requestPath("/api/test")
                .createdAt(createdAt)
                .build();
    }

    private IpAddressRecord createTestRecordWithTag(String tag) {
        return IpAddressRecord.builder()
                .ipAddress("192.168.1.100")
                .userId("user@example.com")
                .tag(tag)
                .httpMethod("GET")
                .requestPath("/api/test")
                .createdAt(testTimestamp)
                .build();
    }

    private IpAddressRecord createTestRecordWithCountry(String countryCode) {
        return IpAddressRecord.builder()
                .ipAddress("192.168.1.100")
                .userId("user@example.com")
                .countryCode(countryCode)
                .httpMethod("GET")
                .requestPath("/api/test")
                .createdAt(testTimestamp)
                .build();
    }
}
