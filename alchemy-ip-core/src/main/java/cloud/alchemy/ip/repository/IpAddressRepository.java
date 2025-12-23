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
package cloud.alchemy.ip.repository;

import cloud.alchemy.ip.entity.IpAddressRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link IpAddressRecord} entity.
 *
 * <p>Provides standard CRUD operations as well as custom query methods
 * for IP address records. This repository can be injected into user code
 * for custom queries and operations.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Service
 * public class IpAnalyticsService {
 *
 *     private final IpAddressRepository ipAddressRepository;
 *
 *     public IpAnalyticsService(IpAddressRepository ipAddressRepository) {
 *         this.ipAddressRepository = ipAddressRepository;
 *     }
 *
 *     public List<IpAddressRecord> getRecentAccessesByUser(String userId) {
 *         return ipAddressRepository.findByUserIdOrderByCreatedAtDesc(userId);
 *     }
 * }
 * }</pre>
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
@Repository
public interface IpAddressRepository extends JpaRepository<IpAddressRecord, Long>,
        JpaSpecificationExecutor<IpAddressRecord> {

    /**
     * Finds all IP address records for a given IP address.
     *
     * @param ipAddress the IP address to search for
     * @return list of matching records
     */
    List<IpAddressRecord> findByIpAddress(String ipAddress);

    /**
     * Finds all IP address records for a given IP address with pagination.
     *
     * @param ipAddress the IP address to search for
     * @param pageable  pagination parameters
     * @return page of matching records
     */
    Page<IpAddressRecord> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * Finds all IP address records for a given user ID.
     *
     * @param userId the user ID to search for
     * @return list of matching records
     */
    List<IpAddressRecord> findByUserId(String userId);

    /**
     * Finds all IP address records for a given user ID, ordered by creation time descending.
     *
     * @param userId the user ID to search for
     * @return list of matching records, newest first
     */
    List<IpAddressRecord> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Finds all IP address records for a given user ID with pagination.
     *
     * @param userId   the user ID to search for
     * @param pageable pagination parameters
     * @return page of matching records
     */
    Page<IpAddressRecord> findByUserId(String userId, Pageable pageable);

    /**
     * Finds IP address records for a specific user and IP combination.
     *
     * @param ipAddress the IP address
     * @param userId    the user ID
     * @return list of matching records
     */
    List<IpAddressRecord> findByIpAddressAndUserId(String ipAddress, String userId);

    /**
     * Finds IP address records created within a time range.
     *
     * @param start    the start of the time range (inclusive)
     * @param end      the end of the time range (inclusive)
     * @param pageable pagination parameters
     * @return page of matching records
     */
    Page<IpAddressRecord> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);

    /**
     * Finds IP address records with a specific tag.
     *
     * @param tag      the tag to search for
     * @param pageable pagination parameters
     * @return page of matching records
     */
    Page<IpAddressRecord> findByTag(String tag, Pageable pageable);

    /**
     * Finds the most recent IP address record for a user.
     *
     * @param userId the user ID
     * @return the most recent record, if any
     */
    Optional<IpAddressRecord> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Finds the most recent IP address record for an IP address.
     *
     * @param ipAddress the IP address
     * @return the most recent record, if any
     */
    Optional<IpAddressRecord> findFirstByIpAddressOrderByCreatedAtDesc(String ipAddress);

    /**
     * Counts the number of records for a given IP address.
     *
     * @param ipAddress the IP address
     * @return the count of records
     */
    long countByIpAddress(String ipAddress);

    /**
     * Counts the number of records for a given user ID.
     *
     * @param userId the user ID
     * @return the count of records
     */
    long countByUserId(String userId);

    /**
     * Counts distinct IP addresses for a user.
     *
     * @param userId the user ID
     * @return the count of distinct IP addresses
     */
    @Query("SELECT COUNT(DISTINCT r.ipAddress) FROM IpAddressRecord r WHERE r.userId = :userId")
    long countDistinctIpAddressesByUserId(@Param("userId") String userId);

    /**
     * Counts distinct users for an IP address.
     *
     * @param ipAddress the IP address
     * @return the count of distinct users
     */
    @Query("SELECT COUNT(DISTINCT r.userId) FROM IpAddressRecord r WHERE r.ipAddress = :ipAddress AND r.userId IS NOT NULL")
    long countDistinctUsersByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Finds distinct IP addresses used by a user.
     *
     * @param userId the user ID
     * @return list of distinct IP addresses
     */
    @Query("SELECT DISTINCT r.ipAddress FROM IpAddressRecord r WHERE r.userId = :userId")
    List<String> findDistinctIpAddressesByUserId(@Param("userId") String userId);

    /**
     * Finds records by country code.
     *
     * @param countryCode the ISO 3166-1 alpha-2 country code
     * @param pageable    pagination parameters
     * @return page of matching records
     */
    Page<IpAddressRecord> findByCountryCode(String countryCode, Pageable pageable);

    /**
     * Checks if an IP address has been recorded for a user.
     *
     * @param ipAddress the IP address
     * @param userId    the user ID
     * @return true if a record exists
     */
    boolean existsByIpAddressAndUserId(String ipAddress, String userId);

    /**
     * Deletes all records older than the specified timestamp.
     *
     * @param timestamp the cutoff timestamp
     * @return the number of deleted records
     */
    long deleteByCreatedAtBefore(Instant timestamp);

    /**
     * Finds IP addresses with access count greater than or equal to the threshold.
     *
     * @param threshold minimum access count
     * @param pageable  pagination parameters
     * @return list of IP addresses with their counts
     */
    @Query("SELECT r.ipAddress, COUNT(r) as cnt FROM IpAddressRecord r " +
            "GROUP BY r.ipAddress HAVING COUNT(r) >= :threshold ORDER BY cnt DESC")
    Page<Object[]> findFrequentIpAddresses(@Param("threshold") long threshold, Pageable pageable);

    /**
     * Finds IP address records matching the request path pattern.
     *
     * @param pathPattern the path pattern (supports SQL LIKE wildcards)
     * @param pageable    pagination parameters
     * @return page of matching records
     */
    Page<IpAddressRecord> findByRequestPathLike(String pathPattern, Pageable pageable);
}
