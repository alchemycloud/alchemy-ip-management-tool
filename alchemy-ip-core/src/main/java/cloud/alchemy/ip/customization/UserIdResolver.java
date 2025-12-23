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
package cloud.alchemy.ip.customization;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * Strategy interface for resolving user IDs from HTTP requests.
 *
 * <p>Implementations of this interface determine how the user ID is extracted
 * from the current request context. This allows applications to customize
 * user identification based on their authentication mechanism.
 *
 * <p>Example implementation using Spring Security:
 * <pre>{@code
 * @Component
 * @Primary
 * public class SecurityContextUserIdResolver implements UserIdResolver {
 *
 *     @Override
 *     public Optional<String> resolveUserId(HttpServletRequest request) {
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         if (auth != null && auth.isAuthenticated()
 *                 && !(auth.getPrincipal() instanceof String
 *                      && "anonymousUser".equals(auth.getPrincipal()))) {
 *             return Optional.of(auth.getName());
 *         }
 *         return Optional.empty();
 *     }
 * }
 * }</pre>
 *
 * <p>Example implementation using JWT from header:
 * <pre>{@code
 * @Component
 * @Primary
 * public class JwtUserIdResolver implements UserIdResolver {
 *
 *     private final JwtDecoder jwtDecoder;
 *
 *     @Override
 *     public Optional<String> resolveUserId(HttpServletRequest request) {
 *         String authHeader = request.getHeader("Authorization");
 *         if (authHeader != null && authHeader.startsWith("Bearer ")) {
 *             String token = authHeader.substring(7);
 *             Jwt jwt = jwtDecoder.decode(token);
 *             return Optional.ofNullable(jwt.getSubject());
 *         }
 *         return Optional.empty();
 *     }
 * }
 * }</pre>
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see DefaultUserIdResolver
 */
@FunctionalInterface
public interface UserIdResolver {

    /**
     * Resolves the user ID from the given HTTP request.
     *
     * @param request the HTTP servlet request
     * @return an {@link Optional} containing the user ID if available,
     *         or {@link Optional#empty()} if the user cannot be identified
     */
    Optional<String> resolveUserId(HttpServletRequest request);
}
