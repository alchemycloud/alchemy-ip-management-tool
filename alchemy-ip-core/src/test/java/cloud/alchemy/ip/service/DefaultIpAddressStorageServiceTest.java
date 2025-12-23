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
package cloud.alchemy.ip.service;

import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.extractor.IpAddressExtractor;
import cloud.alchemy.ip.repository.IpAddressRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultIpAddressStorageService}.
 */
@DisplayName("DefaultIpAddressStorageService")
class DefaultIpAddressStorageServiceTest {

    private StubIpAddressRepository repository;
    private StubIpAddressExtractor ipAddressExtractor;
    private DefaultIpAddressStorageService storageService;

    @BeforeEach
    void setUp() {
        repository = new StubIpAddressRepository();
        ipAddressExtractor = new StubIpAddressExtractor();
        storageService = new DefaultIpAddressStorageService(repository, ipAddressExtractor);
    }

    @Nested
    @DisplayName("store")
    class Store {

        @Test
        @DisplayName("should store new IP address record")
        void shouldStoreNewIpAddressRecord() {
            final IpAddressRecord record = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();

            final Optional<IpAddressRecord> result = storageService.store(record);

            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get().getIpAddress());
            assertEquals(1, repository.savedRecords.size());
        }

        @Test
        @DisplayName("should return empty when IP address already exists for user")
        void shouldReturnEmptyWhenDuplicate() {
            // First, store a record
            final IpAddressRecord existingRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();
            repository.save(existingRecord);

            // Try to store the same IP+user combination
            final IpAddressRecord newRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();

            final Optional<IpAddressRecord> result = storageService.store(newRecord);

            assertTrue(result.isEmpty());
            assertEquals(1, repository.savedRecords.size()); // Only the original record
        }

        @Test
        @DisplayName("should detect duplicate for anonymous user")
        void shouldDetectDuplicateForAnonymousUser() {
            // First, store an anonymous record
            final IpAddressRecord existingRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId(null)
                    .build();
            repository.save(existingRecord);

            // Try to store another anonymous record with same IP
            final IpAddressRecord newRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId(null)
                    .build();

            final Optional<IpAddressRecord> result = storageService.store(newRecord);

            assertTrue(result.isEmpty());
            assertEquals(1, repository.savedRecords.size());
        }

        @Test
        @DisplayName("should store when same IP exists for different user")
        void shouldStoreWhenSameIpExistsForDifferentUser() {
            // First, store a record for one user
            final IpAddressRecord existingRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user1@example.com")
                    .build();
            repository.save(existingRecord);

            // Store another record for different user
            final IpAddressRecord newRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user2@example.com")
                    .build();

            final Optional<IpAddressRecord> result = storageService.store(newRecord);

            assertTrue(result.isPresent());
            assertEquals(2, repository.savedRecords.size());
        }

        @Test
        @DisplayName("should allow anonymous when existing records have users")
        void shouldAllowAnonymousWhenExistingRecordsHaveUsers() {
            // First, store a record with a user
            final IpAddressRecord existingRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();
            repository.save(existingRecord);

            // Store an anonymous record with same IP
            final IpAddressRecord newRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId(null)
                    .build();

            final Optional<IpAddressRecord> result = storageService.store(newRecord);

            assertTrue(result.isPresent());
            assertEquals(2, repository.savedRecords.size());
        }

        @Test
        @DisplayName("should throw exception when record is null")
        void shouldThrowExceptionWhenRecordIsNull() {
            assertThrows(IllegalArgumentException.class, () -> storageService.store(null));
        }
    }

    @Nested
    @DisplayName("storeAsync")
    class StoreAsync {

        @Test
        @DisplayName("should store record asynchronously")
        void shouldStoreRecordAsynchronously() throws Exception {
            final IpAddressRecord record = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();

            final CompletableFuture<Optional<IpAddressRecord>> future = storageService.storeAsync(record);
            final Optional<IpAddressRecord> result = future.get();

            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get().getIpAddress());
        }

        @Test
        @DisplayName("should return empty future when duplicate")
        void shouldReturnEmptyFutureWhenDuplicate() throws Exception {
            // First, store a record
            final IpAddressRecord existingRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();
            repository.save(existingRecord);

            // Try to store duplicate asynchronously
            final IpAddressRecord newRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();

            final CompletableFuture<Optional<IpAddressRecord>> future = storageService.storeAsync(newRecord);
            final Optional<IpAddressRecord> result = future.get();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("storeFromRequest")
    class StoreFromRequest {

        @Test
        @DisplayName("should create and store record from request")
        void shouldCreateAndStoreRecordFromRequest() {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("User-Agent", "Mozilla/5.0");
            request.setRequestURI("/api/test");
            request.setMethod("POST");

            ipAddressExtractor.setIpAddress("203.0.113.195");

            final Optional<IpAddressRecord> result = storageService.storeFromRequest(request, "user@example.com");

            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get().getIpAddress());
            assertEquals("user@example.com", result.get().getUserId());
            assertEquals("Mozilla/5.0", result.get().getUserAgent());
            assertEquals("/api/test", result.get().getRequestPath());
            assertEquals("POST", result.get().getHttpMethod());
        }

        @Test
        @DisplayName("should return empty when duplicate from request")
        void shouldReturnEmptyWhenDuplicateFromRequest() {
            // First, store a record
            final IpAddressRecord existingRecord = IpAddressRecord.builder()
                    .ipAddress("203.0.113.195")
                    .userId("user@example.com")
                    .build();
            repository.save(existingRecord);

            final MockHttpServletRequest request = new MockHttpServletRequest();
            ipAddressExtractor.setIpAddress("203.0.113.195");

            final Optional<IpAddressRecord> result = storageService.storeFromRequest(request, "user@example.com");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> storageService.storeFromRequest(null, "user@example.com"));
        }

        @Test
        @DisplayName("should throw exception when IP cannot be extracted")
        void shouldThrowExceptionWhenIpCannotBeExtracted() {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            ipAddressExtractor.setIpAddress(null); // No IP available

            assertThrows(IllegalStateException.class,
                    () -> storageService.storeFromRequest(request, "user@example.com"));
        }
    }

    @Nested
    @DisplayName("storeFromRequestAsync")
    class StoreFromRequestAsync {

        @Test
        @DisplayName("should store from request asynchronously")
        void shouldStoreFromRequestAsynchronously() throws Exception {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("User-Agent", "Mozilla/5.0");
            ipAddressExtractor.setIpAddress("203.0.113.195");

            final CompletableFuture<Optional<IpAddressRecord>> future =
                    storageService.storeFromRequestAsync(request, "user@example.com");
            final Optional<IpAddressRecord> result = future.get();

            assertTrue(result.isPresent());
            assertEquals("203.0.113.195", result.get().getIpAddress());
        }

        @Test
        @DisplayName("should return failed future when IP cannot be extracted")
        void shouldReturnFailedFutureWhenIpCannotBeExtracted() {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            ipAddressExtractor.setIpAddress(null);

            final CompletableFuture<Optional<IpAddressRecord>> future =
                    storageService.storeFromRequestAsync(request, "user@example.com");

            assertTrue(future.isCompletedExceptionally());
        }
    }

    // Stub implementations for testing

    private static class StubIpAddressExtractor implements IpAddressExtractor {
        private String ipAddress = "127.0.0.1";

        void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        @Override
        public Optional<String> extractIpAddress(HttpServletRequest request) {
            return Optional.ofNullable(ipAddress);
        }
    }

    private static class StubIpAddressRepository implements IpAddressRepository {
        final List<IpAddressRecord> savedRecords = new ArrayList<>();
        private final AtomicLong idGenerator = new AtomicLong(1);

        @Override
        public List<IpAddressRecord> findByIpAddress(String ipAddress) {
            return savedRecords.stream()
                    .filter(r -> ipAddress.equals(r.getIpAddress()))
                    .toList();
        }

        @Override
        public boolean existsByIpAddressAndUserId(String ipAddress, String userId) {
            return savedRecords.stream()
                    .anyMatch(r -> ipAddress.equals(r.getIpAddress()) &&
                             userId != null && userId.equals(r.getUserId()));
        }

        @Override
        public <S extends IpAddressRecord> S save(S entity) {
            savedRecords.add(entity);
            return entity;
        }

        // Unused methods - minimal implementations

        @Override
        public Page<IpAddressRecord> findByIpAddress(String ipAddress, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public List<IpAddressRecord> findByUserId(String userId) {
            return List.of();
        }

        @Override
        public List<IpAddressRecord> findByUserIdOrderByCreatedAtDesc(String userId) {
            return List.of();
        }

        @Override
        public Page<IpAddressRecord> findByUserId(String userId, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public List<IpAddressRecord> findByIpAddressAndUserId(String ipAddress, String userId) {
            return List.of();
        }

        @Override
        public Page<IpAddressRecord> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Page<IpAddressRecord> findByTag(String tag, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Optional<IpAddressRecord> findFirstByUserIdOrderByCreatedAtDesc(String userId) {
            return Optional.empty();
        }

        @Override
        public Optional<IpAddressRecord> findFirstByIpAddressOrderByCreatedAtDesc(String ipAddress) {
            return Optional.empty();
        }

        @Override
        public long countByIpAddress(String ipAddress) {
            return 0;
        }

        @Override
        public long countByUserId(String userId) {
            return 0;
        }

        @Override
        public long countDistinctIpAddressesByUserId(String userId) {
            return 0;
        }

        @Override
        public long countDistinctUsersByIpAddress(String ipAddress) {
            return 0;
        }

        @Override
        public List<String> findDistinctIpAddressesByUserId(String userId) {
            return List.of();
        }

        @Override
        public Page<IpAddressRecord> findByCountryCode(String countryCode, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public long deleteByCreatedAtBefore(Instant timestamp) {
            return 0;
        }

        @Override
        public Page<Object[]> findFrequentIpAddresses(long threshold, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Page<IpAddressRecord> findByRequestPathLike(String pathPattern, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public void flush() {}

        @Override
        public <S extends IpAddressRecord> S saveAndFlush(S entity) {
            return save(entity);
        }

        @Override
        public <S extends IpAddressRecord> List<S> saveAllAndFlush(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public void deleteAllInBatch(Iterable<IpAddressRecord> entities) {}

        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {}

        @Override
        public void deleteAllInBatch() {}

        @Override
        public IpAddressRecord getOne(Long aLong) {
            return null;
        }

        @Override
        public IpAddressRecord getById(Long aLong) {
            return null;
        }

        @Override
        public IpAddressRecord getReferenceById(Long aLong) {
            return null;
        }

        @Override
        public <S extends IpAddressRecord> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends IpAddressRecord> List<S> findAll(Example<S> example) {
            return List.of();
        }

        @Override
        public <S extends IpAddressRecord> List<S> findAll(Example<S> example, Sort sort) {
            return List.of();
        }

        @Override
        public <S extends IpAddressRecord> Page<S> findAll(Example<S> example, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public <S extends IpAddressRecord> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends IpAddressRecord> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends IpAddressRecord, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public Optional<IpAddressRecord> findOne(Specification<IpAddressRecord> spec) {
            return Optional.empty();
        }

        @Override
        public List<IpAddressRecord> findAll(Specification<IpAddressRecord> spec) {
            return List.of();
        }

        @Override
        public Page<IpAddressRecord> findAll(Specification<IpAddressRecord> spec, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public List<IpAddressRecord> findAll(Specification<IpAddressRecord> spec, Sort sort) {
            return List.of();
        }

        @Override
        public long count(Specification<IpAddressRecord> spec) {
            return 0;
        }

        @Override
        public boolean exists(Specification<IpAddressRecord> spec) {
            return false;
        }

        @Override
        public long delete(Specification<IpAddressRecord> spec) {
            return 0;
        }

        @Override
        public <S extends IpAddressRecord, R> R findBy(Specification<IpAddressRecord> spec, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public <S extends IpAddressRecord> List<S> saveAll(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public Optional<IpAddressRecord> findById(Long aLong) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Long aLong) {
            return false;
        }

        @Override
        public List<IpAddressRecord> findAll() {
            return new ArrayList<>(savedRecords);
        }

        @Override
        public List<IpAddressRecord> findAllById(Iterable<Long> longs) {
            return List.of();
        }

        @Override
        public long count() {
            return savedRecords.size();
        }

        @Override
        public void deleteById(Long aLong) {}

        @Override
        public void delete(IpAddressRecord entity) {}

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {}

        @Override
        public void deleteAll(Iterable<? extends IpAddressRecord> entities) {}

        @Override
        public void deleteAll() {
            savedRecords.clear();
        }

        @Override
        public List<IpAddressRecord> findAll(Sort sort) {
            return new ArrayList<>(savedRecords);
        }

        @Override
        public Page<IpAddressRecord> findAll(Pageable pageable) {
            return Page.empty();
        }
    }
}
