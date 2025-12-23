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

import cloud.alchemy.ip.api.dto.*;
import cloud.alchemy.ip.api.mapper.IpRecordMapper;
import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.repository.IpAddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for IP statistics and analytics.
 */
@RestController
@RequestMapping("${alchemy.ip.api.base-path:/api/alchemy-ip}")
public class IpStatsController {

    private final IpAddressRepository repository;
    private final IpRecordMapper mapper;

    public IpStatsController(IpAddressRepository repository, IpRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Get summary statistics for the dashboard.
     */
    @GetMapping("/stats/summary")
    public IpStatsDto getSummaryStats() {
        long totalRecords = repository.count();

        // Get unique counts
        List<IpAddressRecord> allRecords = repository.findAll();
        long uniqueIps = allRecords.stream()
                .map(IpAddressRecord::getIpAddress)
                .distinct()
                .count();
        long uniqueUsers = allRecords.stream()
                .map(IpAddressRecord::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // Records today and this week
        Instant now = Instant.now();
        Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant startOfWeek = LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant();

        long recordsToday = repository.findByCreatedAtBetween(startOfToday, now, PageRequest.of(0, 1))
                .getTotalElements();
        long recordsThisWeek = repository.findByCreatedAtBetween(startOfWeek, now, PageRequest.of(0, 1))
                .getTotalElements();

        // Top countries
        Map<String, Long> topCountries = allRecords.stream()
                .filter(r -> r.getCountryCode() != null && !r.getCountryCode().isBlank())
                .collect(Collectors.groupingBy(IpAddressRecord::getCountryCode, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // Top IP addresses
        Page<Object[]> frequentIps = repository.findFrequentIpAddresses(1, PageRequest.of(0, 10));
        List<FrequentIpDto> topIps = frequentIps.getContent().stream()
                .map(arr -> new FrequentIpDto((String) arr[0], (Long) arr[1]))
                .toList();

        // Recent records
        List<IpRecordDto> recentRecords = repository.findAll(
                        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(mapper::toDto)
                .toList();

        // Oldest and newest timestamps
        Instant oldestRecord = allRecords.stream()
                .map(IpAddressRecord::getCreatedAt)
                .min(Instant::compareTo)
                .orElse(null);
        Instant newestRecord = allRecords.stream()
                .map(IpAddressRecord::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        return new IpStatsDto(
                totalRecords,
                uniqueIps,
                uniqueUsers,
                recordsToday,
                recordsThisWeek,
                topCountries,
                topIps,
                recentRecords,
                oldestRecord,
                newestRecord
        );
    }

    /**
     * Get timeline data for charts.
     */
    @GetMapping("/stats/timeline")
    public TimelineDataDto getTimelineData(
            @RequestParam(defaultValue = "30") int days) {

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<LocalDate> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Instant dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            long count = repository.findByCreatedAtBetween(dayStart, dayEnd, PageRequest.of(0, 1))
                    .getTotalElements();

            labels.add(date);
            counts.add(count);
        }

        return new TimelineDataDto(labels, counts);
    }

    /**
     * Get geographic distribution.
     */
    @GetMapping("/stats/geographic")
    public GeoDistributionDto getGeoDistribution() {
        List<IpAddressRecord> allRecords = repository.findAll();

        Map<String, Long> countryDistribution = allRecords.stream()
                .filter(r -> r.getCountryCode() != null && !r.getCountryCode().isBlank())
                .collect(Collectors.groupingBy(IpAddressRecord::getCountryCode, Collectors.counting()));

        Map<String, Long> cityDistribution = allRecords.stream()
                .filter(r -> r.getCity() != null && !r.getCity().isBlank())
                .collect(Collectors.groupingBy(IpAddressRecord::getCity, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        return new GeoDistributionDto(countryDistribution, cityDistribution);
    }

    /**
     * Get frequent IP addresses.
     */
    @GetMapping("/stats/frequent")
    public PageResponseDto<FrequentIpDto> getFrequentIps(
            @RequestParam(defaultValue = "2") long threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Object[]> frequentIps = repository.findFrequentIpAddresses(threshold, pageable);

        List<FrequentIpDto> content = frequentIps.getContent().stream()
                .map(arr -> new FrequentIpDto((String) arr[0], (Long) arr[1]))
                .toList();

        return new PageResponseDto<>(
                content,
                frequentIps.getNumber(),
                frequentIps.getSize(),
                frequentIps.getTotalElements(),
                frequentIps.getTotalPages(),
                frequentIps.isFirst(),
                frequentIps.isLast()
        );
    }

    /**
     * Get count by IP address.
     */
    @GetMapping("/stats/ip/{ipAddress}/count")
    public Map<String, Long> getCountByIp(@PathVariable String ipAddress) {
        long count = repository.countByIpAddress(ipAddress);
        return Map.of("count", count);
    }

    /**
     * Get count by user ID.
     */
    @GetMapping("/stats/user/{userId}/count")
    public Map<String, Long> getCountByUser(@PathVariable String userId) {
        long count = repository.countByUserId(userId);
        return Map.of("count", count);
    }

    /**
     * Get distinct IP count for a user.
     */
    @GetMapping("/stats/user/{userId}/distinct-ips")
    public Map<String, Object> getDistinctIpsForUser(@PathVariable String userId) {
        long count = repository.countDistinctIpAddressesByUserId(userId);
        List<String> ips = repository.findDistinctIpAddressesByUserId(userId);
        return Map.of("count", count, "ipAddresses", ips);
    }

    /**
     * Get distinct user count for an IP.
     */
    @GetMapping("/stats/ip/{ipAddress}/distinct-users")
    public Map<String, Long> getDistinctUsersForIp(@PathVariable String ipAddress) {
        long count = repository.countDistinctUsersByIpAddress(ipAddress);
        return Map.of("count", count);
    }
}
