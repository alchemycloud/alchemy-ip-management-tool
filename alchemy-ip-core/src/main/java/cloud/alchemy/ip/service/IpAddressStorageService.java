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
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for storing IP address records.
 *
 * <p>This interface defines the contract for persisting IP address information.
 * Users can provide custom implementations to override the default persistence
 * behavior or add additional processing logic.
 *
 * <p>Implementations should skip storing duplicate IP addresses (same IP + user combination).
 *
 * <p>Example of custom implementation:
 * <pre>{@code
 * @Service
 * @Primary
 * public class CustomIpAddressStorageService implements IpAddressStorageService {
 *
 *     private final IpAddressRepository repository;
 *     private final AuditService auditService;
 *
 *     @Override
 *     public Optional<IpAddressRecord> store(IpAddressRecord record) {
 *         // Add custom audit logging
 *         auditService.logIpAccess(record);
 *         return Optional.of(repository.save(record));
 *     }
 *
 *     @Override
 *     public CompletableFuture<Optional<IpAddressRecord>> storeAsync(IpAddressRecord record) {
 *         return CompletableFuture.supplyAsync(() -> store(record));
 *     }
 * }
 * }</pre>
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see DefaultIpAddressStorageService
 */
public interface IpAddressStorageService {

    /**
     * Stores an IP address record synchronously.
     *
     * <p>If the IP address already exists for the given user, the record is not stored
     * and an empty Optional is returned.
     *
     * @param record the IP address record to store
     * @return an Optional containing the stored record, or empty if duplicate
     */
    Optional<IpAddressRecord> store(IpAddressRecord record);

    /**
     * Stores an IP address record asynchronously.
     *
     * <p>If the IP address already exists for the given user, the record is not stored
     * and a CompletableFuture with empty Optional is returned.
     *
     * @param record the IP address record to store
     * @return a CompletableFuture that completes with an Optional containing the stored record,
     *         or empty if duplicate
     */
    CompletableFuture<Optional<IpAddressRecord>> storeAsync(IpAddressRecord record);

    /**
     * Creates and stores an IP address record from the HTTP request.
     *
     * <p>If the IP address already exists for the given user, the record is not stored
     * and an empty Optional is returned.
     *
     * @param request the HTTP servlet request
     * @param userId  the user ID (may be null)
     * @return an Optional containing the stored record, or empty if duplicate
     */
    Optional<IpAddressRecord> storeFromRequest(HttpServletRequest request, String userId);

    /**
     * Creates and stores an IP address record from the HTTP request asynchronously.
     *
     * <p>If the IP address already exists for the given user, the record is not stored
     * and a CompletableFuture with empty Optional is returned.
     *
     * @param request the HTTP servlet request
     * @param userId  the user ID (may be null)
     * @return a CompletableFuture that completes with an Optional containing the stored record,
     *         or empty if duplicate
     */
    CompletableFuture<Optional<IpAddressRecord>> storeFromRequestAsync(HttpServletRequest request, String userId);
}
