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
 * Full detail DTO for IP address records.
 *
 * @param id           the record ID
 * @param ipAddress    the IP address
 * @param userId       the user ID (may be null)
 * @param userAgent    the user agent string (may be null)
 * @param requestPath  the request path (may be null)
 * @param httpMethod   the HTTP method (may be null)
 * @param tag          the tag (may be null)
 * @param countryCode  the country code (may be null)
 * @param city         the city (may be null)
 * @param region       the region (may be null)
 * @param latitude     the latitude (may be null)
 * @param longitude    the longitude (may be null)
 * @param sourceHeader the source header used to extract IP (may be null)
 * @param createdAt    the creation timestamp
 * @param metadata     custom metadata JSON (may be null)
 */
public record IpRecordDetailDto(
        Long id,
        String ipAddress,
        String userId,
        String userAgent,
        String requestPath,
        String httpMethod,
        String tag,
        String countryCode,
        String city,
        String region,
        Double latitude,
        Double longitude,
        String sourceHeader,
        Instant createdAt,
        String metadata
) {
}
