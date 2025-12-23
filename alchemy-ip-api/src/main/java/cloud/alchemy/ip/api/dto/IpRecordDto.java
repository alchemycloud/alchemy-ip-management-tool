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
 * Lightweight DTO for IP address records in list views.
 *
 * @param id          the record ID
 * @param ipAddress   the IP address
 * @param userId      the user ID (may be null)
 * @param httpMethod  the HTTP method
 * @param requestPath the request path
 * @param tag         the tag (may be null)
 * @param countryCode the country code (may be null)
 * @param createdAt   the creation timestamp
 */
public record IpRecordDto(
        Long id,
        String ipAddress,
        String userId,
        String httpMethod,
        String requestPath,
        String tag,
        String countryCode,
        Instant createdAt
) {
}
