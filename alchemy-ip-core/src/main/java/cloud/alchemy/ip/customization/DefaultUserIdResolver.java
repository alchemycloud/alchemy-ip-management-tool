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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;
import java.util.Optional;

/**
 * Default implementation of {@link UserIdResolver} that extracts the user's email
 * from Spring Security's {@link Authentication} context.
 *
 * <p>This implementation tries the following sources in order:
 * <ol>
 *   <li>Spring Security's {@link SecurityContextHolder} - extracts email from various principal types</li>
 *   <li>Request's user principal name ({@link HttpServletRequest#getUserPrincipal()})</li>
 *   <li>Remote user from the request ({@link HttpServletRequest#getRemoteUser()})</li>
 * </ol>
 *
 * <p>Supported principal types:
 * <ul>
 *   <li>{@link OidcUser} - extracts email claim</li>
 *   <li>{@link OAuth2User} - extracts email attribute</li>
 *   <li>{@link UserDetails} - uses username (typically email)</li>
 *   <li>String principal - uses directly if it looks like an email</li>
 * </ul>
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
public final class DefaultUserIdResolver implements UserIdResolver {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserIdResolver.class);
    private static final String EMAIL_CLAIM = "email";

    @Override
    public Optional<String> resolveUserId(HttpServletRequest request) {
        // First, try Spring Security context
        final Optional<String> securityEmail = extractFromSecurityContext();
        if (securityEmail.isPresent()) {
            return securityEmail;
        }

        // Fallback to request principal
        if (request != null) {
            final Principal principal = request.getUserPrincipal();
            if (principal != null && isValidEmail(principal.getName())) {
                log.debug("Resolved user email from request principal: {}", principal.getName());
                return Optional.of(principal.getName());
            }

            final String remoteUser = request.getRemoteUser();
            if (isValidEmail(remoteUser)) {
                log.debug("Resolved user email from remote user: {}", remoteUser);
                return Optional.of(remoteUser);
            }
        }

        log.debug("Could not resolve user email from request");
        return Optional.empty();
    }

    private Optional<String> extractFromSecurityContext() {
        try {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            final Object principal = authentication.getPrincipal();

            // Handle anonymous user
            if ("anonymousUser".equals(principal)) {
                return Optional.empty();
            }

            // Try OIDC user (has email claim)
            if (principal instanceof OidcUser oidcUser) {
                final String email = oidcUser.getEmail();
                if (isValidEmail(email)) {
                    log.debug("Resolved user email from OidcUser: {}", email);
                    return Optional.of(email);
                }
            }

            // Try OAuth2 user (may have email attribute)
            if (principal instanceof OAuth2User oauth2User) {
                final Object emailAttr = oauth2User.getAttribute(EMAIL_CLAIM);
                if (emailAttr instanceof String email && isValidEmail(email)) {
                    log.debug("Resolved user email from OAuth2User: {}", email);
                    return Optional.of(email);
                }
            }

            // Try UserDetails (username is often email)
            if (principal instanceof UserDetails userDetails) {
                final String username = userDetails.getUsername();
                if (isValidEmail(username)) {
                    log.debug("Resolved user email from UserDetails: {}", username);
                    return Optional.of(username);
                }
            }

            // Try authentication name directly
            final String name = authentication.getName();
            if (isValidEmail(name)) {
                log.debug("Resolved user email from authentication name: {}", name);
                return Optional.of(name);
            }

            return Optional.empty();
        } catch (Exception e) {
            log.debug("Error extracting email from security context: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isValidEmail(String value) {
        return value != null && !value.isBlank() && value.contains("@") && value.contains(".");
    }
}
