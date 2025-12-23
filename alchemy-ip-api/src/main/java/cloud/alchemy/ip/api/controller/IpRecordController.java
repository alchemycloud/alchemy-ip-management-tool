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

import cloud.alchemy.ip.api.dto.IpRecordDetailDto;
import cloud.alchemy.ip.api.dto.IpRecordDto;
import cloud.alchemy.ip.api.dto.PageResponseDto;
import cloud.alchemy.ip.api.dto.SearchCriteriaDto;
import cloud.alchemy.ip.api.exception.RecordNotFoundException;
import cloud.alchemy.ip.api.mapper.IpRecordMapper;
import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.repository.IpAddressRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST controller for IP address record operations.
 */
@RestController
@RequestMapping("${alchemy.ip.api.base-path:/api/alchemy-ip}")
public class IpRecordController {

    private final IpAddressRepository repository;
    private final IpRecordMapper mapper;

    public IpRecordController(IpAddressRepository repository, IpRecordMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Get all IP records with pagination.
     */
    @GetMapping("/records")
    public PageResponseDto<IpRecordDto> getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortBy));
        Page<IpAddressRecord> records = repository.findAll(pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Get a single IP record by ID.
     */
    @GetMapping("/records/{id}")
    public IpRecordDetailDto getRecordById(@PathVariable Long id) {
        return repository.findById(id)
                .map(mapper::toDetailDto)
                .orElseThrow(() -> new RecordNotFoundException(id));
    }

    /**
     * Get IP records by IP address.
     */
    @GetMapping("/records/ip/{ipAddress}")
    public PageResponseDto<IpRecordDto> getRecordsByIp(
            @PathVariable String ipAddress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IpAddressRecord> records = repository.findByIpAddress(ipAddress, pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Get IP records by user ID.
     */
    @GetMapping("/records/user/{userId}")
    public PageResponseDto<IpRecordDto> getRecordsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IpAddressRecord> records = repository.findByUserId(userId, pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Get IP records by tag.
     */
    @GetMapping("/records/tag/{tag}")
    public PageResponseDto<IpRecordDto> getRecordsByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IpAddressRecord> records = repository.findByTag(tag, pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Get IP records by country code.
     */
    @GetMapping("/records/country/{countryCode}")
    public PageResponseDto<IpRecordDto> getRecordsByCountry(
            @PathVariable String countryCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IpAddressRecord> records = repository.findByCountryCode(countryCode.toUpperCase(), pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Get IP records within a date range.
     */
    @GetMapping("/records/date-range")
    public PageResponseDto<IpRecordDto> getRecordsByDateRange(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<IpAddressRecord> records = repository.findByCreatedAtBetween(startDate, endDate, pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Advanced search with multiple criteria.
     */
    @PostMapping("/records/search")
    public PageResponseDto<IpRecordDto> searchRecords(
            @RequestBody SearchCriteriaDto criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortBy));

        Specification<IpAddressRecord> spec = buildSpecification(criteria);
        Page<IpAddressRecord> records = repository.findAll(spec, pageable);
        return PageResponseDto.from(records, mapper::toDto);
    }

    /**
     * Delete records older than a timestamp.
     */
    @DeleteMapping("/records/before/{timestamp}")
    public ResponseEntity<Map<String, Long>> deleteOldRecords(@PathVariable Instant timestamp) {
        long deletedCount = repository.deleteByCreatedAtBefore(timestamp);
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount));
    }

    private Specification<IpAddressRecord> buildSpecification(SearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.ipAddress() != null && !criteria.ipAddress().isBlank()) {
                predicates.add(cb.equal(root.get("ipAddress"), criteria.ipAddress()));
            }
            if (criteria.userId() != null && !criteria.userId().isBlank()) {
                predicates.add(cb.equal(root.get("userId"), criteria.userId()));
            }
            if (criteria.tag() != null && !criteria.tag().isBlank()) {
                predicates.add(cb.equal(root.get("tag"), criteria.tag()));
            }
            if (criteria.countryCode() != null && !criteria.countryCode().isBlank()) {
                predicates.add(cb.equal(root.get("countryCode"), criteria.countryCode().toUpperCase()));
            }
            if (criteria.httpMethod() != null && !criteria.httpMethod().isBlank()) {
                predicates.add(cb.equal(root.get("httpMethod"), criteria.httpMethod().toUpperCase()));
            }
            if (criteria.requestPathPattern() != null && !criteria.requestPathPattern().isBlank()) {
                predicates.add(cb.like(root.get("requestPath"), criteria.requestPathPattern()));
            }
            if (criteria.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.startDate()));
            }
            if (criteria.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.endDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
