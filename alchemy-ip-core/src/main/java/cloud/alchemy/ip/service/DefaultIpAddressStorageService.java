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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of {@link IpAddressStorageService}.
 *
 * <p>This service handles the persistence of IP address records using
 * the configured repository and IP address extractor.
 *
 * <p>Duplicate IP addresses (same IP + user combination) are not stored.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
public class DefaultIpAddressStorageService implements IpAddressStorageService {

    private static final Logger log = LoggerFactory.getLogger(DefaultIpAddressStorageService.class);

    private final IpAddressRepository repository;
    private final IpAddressExtractor ipAddressExtractor;

    /**
     * Creates a new storage service with the specified dependencies.
     *
     * @param repository         the IP address repository
     * @param ipAddressExtractor the IP address extractor
     */
    public DefaultIpAddressStorageService(IpAddressRepository repository,
                                          IpAddressExtractor ipAddressExtractor) {
        this.repository = repository;
        this.ipAddressExtractor = ipAddressExtractor;
    }

    @Override
    public Optional<IpAddressRecord> store(IpAddressRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("IpAddressRecord cannot be null");
        }
        if (record.getIpAddress() == null || record.getIpAddress().isBlank()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }

        // Check for duplicate: same IP + user combination
        if (isDuplicate(record.getIpAddress(), record.getUserId())) {
            log.debug("Skipping duplicate IP address record: ip={}, userId={}",
                    record.getIpAddress(), record.getUserId());
            return Optional.empty();
        }

        log.debug("Storing IP address record: {}", record);
        final IpAddressRecord saved = repository.save(record);
        log.debug("Successfully stored IP address record with ID: {}", saved.getId());
        return Optional.of(saved);
    }

    @Override
    @Async("ipManagementTaskExecutor")
    public CompletableFuture<Optional<IpAddressRecord>> storeAsync(IpAddressRecord record) {
        try {
            final Optional<IpAddressRecord> saved = store(record);
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            log.error("Failed to store IP address record asynchronously", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public Optional<IpAddressRecord> storeFromRequest(HttpServletRequest request, String userId) {
        if (request == null) {
            throw new IllegalArgumentException("HttpServletRequest cannot be null");
        }

        final String ipAddress = ipAddressExtractor.extractIpAddress(request)
                .orElseThrow(() -> new IllegalStateException("Could not extract IP address from request"));

        final IpAddressRecord record = IpAddressRecord.builder()
                .ipAddress(ipAddress)
                .userId(userId)
                .userAgent(request.getHeader("User-Agent"))
                .requestPath(request.getRequestURI())
                .httpMethod(request.getMethod())
                .build();

        return store(record);
    }

    @Override
    @Async("ipManagementTaskExecutor")
    public CompletableFuture<Optional<IpAddressRecord>> storeFromRequestAsync(HttpServletRequest request, String userId) {
        try {
            final Optional<IpAddressRecord> saved = storeFromRequest(request, userId);
            return CompletableFuture.completedFuture(saved);
        } catch (Exception e) {
            log.error("Failed to store IP address record from request asynchronously", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private boolean isDuplicate(String ipAddress, String userId) {
        if (userId == null) {
            // For anonymous users, check if IP already exists without user
            return repository.findByIpAddress(ipAddress).stream()
                    .anyMatch(record -> record.getUserId() == null);
        }
        return repository.existsByIpAddressAndUserId(ipAddress, userId);
    }
}
