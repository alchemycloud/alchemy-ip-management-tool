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
package cloud.alchemy.ip.extractor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Strategy interface for extracting client IP addresses from HTTP requests.
 *
 * <p>Implementations of this interface handle the extraction of the real client
 * IP address from various deployment environments (cloud providers, reverse proxies, etc.).
 *
 * <p>Users can provide custom implementations to handle specific deployment scenarios
 * or integrate with custom proxy configurations.
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see DefaultIpAddressExtractor
 */
@FunctionalInterface
public interface IpAddressExtractor {

    /**
     * Extracts the client IP address from the given HTTP request.
     *
     * @param request the HTTP servlet request
     * @return an {@link Optional} containing the extracted IP address,
     *         or {@link Optional#empty()} if the IP address cannot be determined
     */
    Optional<String> extractIpAddress(HttpServletRequest request);
}
