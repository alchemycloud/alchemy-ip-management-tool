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
package cloud.alchemy.ip.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for dashboard summary statistics.
 *
 * @param totalRecords      total number of IP records
 * @param uniqueIpAddresses count of unique IP addresses
 * @param uniqueUsers       count of unique users
 * @param recordsToday      records created today
 * @param recordsThisWeek   records created this week
 * @param topCountries      top countries by record count
 * @param topIpAddresses    top IP addresses by record count
 * @param recentRecords     most recent records
 * @param oldestRecord      timestamp of oldest record
 * @param newestRecord      timestamp of newest record
 */
public record IpStatsDto(
        long totalRecords,
        long uniqueIpAddresses,
        long uniqueUsers,
        long recordsToday,
        long recordsThisWeek,
        Map<String, Long> topCountries,
        List<FrequentIpDto> topIpAddresses,
        List<IpRecordDto> recentRecords,
        Instant oldestRecord,
        Instant newestRecord
) {
}
