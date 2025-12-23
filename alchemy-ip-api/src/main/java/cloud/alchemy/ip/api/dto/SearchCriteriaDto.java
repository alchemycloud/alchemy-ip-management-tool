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

/**
 * DTO for advanced search criteria.
 *
 * @param ipAddress          filter by IP address (exact match)
 * @param userId             filter by user ID (exact match)
 * @param tag                filter by tag (exact match)
 * @param countryCode        filter by country code (exact match)
 * @param httpMethod         filter by HTTP method (exact match)
 * @param requestPathPattern filter by request path pattern (SQL LIKE)
 * @param startDate          filter by creation date start (inclusive)
 * @param endDate            filter by creation date end (inclusive)
 */
public record SearchCriteriaDto(
        String ipAddress,
        String userId,
        String tag,
        String countryCode,
        String httpMethod,
        String requestPathPattern,
        Instant startDate,
        Instant endDate
) {
}
