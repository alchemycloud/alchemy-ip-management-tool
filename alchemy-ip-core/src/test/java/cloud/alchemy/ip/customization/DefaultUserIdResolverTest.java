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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DefaultUserIdResolver}.
 */
@DisplayName("DefaultUserIdResolver")
class DefaultUserIdResolverTest {

    private DefaultUserIdResolver resolver;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        resolver = new DefaultUserIdResolver();
        request = new MockHttpServletRequest();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(Object principal) {
        setAuthentication(principal, true);
    }

    private void setAuthentication(Object principal, boolean authenticated) {
        final Authentication auth = new TestingAuthenticationToken(principal, "credentials");
        ((TestingAuthenticationToken) auth).setAuthenticated(authenticated);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("resolveUserId from Security Context")
    class ResolveFromSecurityContext {

        @Test
        @DisplayName("should extract email from OidcUser")
        void shouldExtractEmailFromOidcUser() {
            final Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "12345");
            claims.put("email", "user@example.com");

            final OidcIdToken idToken = new OidcIdToken(
                    "token-value",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    claims
            );

            final OidcUser oidcUser = new DefaultOidcUser(Collections.emptyList(), idToken);
            setAuthentication(oidcUser);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("user@example.com", result.get());
        }

        @Test
        @DisplayName("should extract email from OAuth2User")
        void shouldExtractEmailFromOAuth2User() {
            final Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "12345");
            attributes.put("email", "oauth@example.com");

            final OAuth2User oauth2User = new DefaultOAuth2User(
                    Collections.emptyList(),
                    attributes,
                    "sub"
            );
            setAuthentication(oauth2User);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("oauth@example.com", result.get());
        }

        @Test
        @DisplayName("should extract email from UserDetails")
        void shouldExtractEmailFromUserDetails() {
            final UserDetails userDetails = User.builder()
                    .username("admin@example.com")
                    .password("password")
                    .authorities(Collections.emptyList())
                    .build();
            setAuthentication(userDetails);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("admin@example.com", result.get());
        }

        @Test
        @DisplayName("should extract email from authentication name")
        void shouldExtractEmailFromAuthenticationName() {
            // Use a string principal with a valid email as the authentication name
            final Authentication auth = new TestingAuthenticationToken("some-principal", "credentials") {
                @Override
                public String getName() {
                    return "name@example.com";
                }
            };
            auth.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(auth);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("name@example.com", result.get());
        }

        @Test
        @DisplayName("should return empty for anonymous user")
        void shouldReturnEmptyForAnonymousUser() {
            setAuthentication("anonymousUser");

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty when not authenticated")
        void shouldReturnEmptyWhenNotAuthenticated() {
            setAuthentication("user@example.com", false);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty when authentication is null")
        void shouldReturnEmptyWhenAuthenticationIsNull() {
            SecurityContextHolder.getContext().setAuthentication(null);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should skip UserDetails with non-email username")
        void shouldSkipUserDetailsWithNonEmailUsername() {
            final UserDetails userDetails = User.builder()
                    .username("admin")
                    .password("password")
                    .authorities(Collections.emptyList())
                    .build();
            setAuthentication(userDetails);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should skip OAuth2User without email attribute")
        void shouldSkipOAuth2UserWithoutEmailAttribute() {
            final Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "12345");
            attributes.put("name", "John Doe");

            final OAuth2User oauth2User = new DefaultOAuth2User(
                    Collections.emptyList(),
                    attributes,
                    "sub"
            );
            setAuthentication(oauth2User);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("resolveUserId from Request")
    class ResolveFromRequest {

        @Test
        @DisplayName("should extract email from request principal")
        void shouldExtractEmailFromRequestPrincipal() {
            final Principal principal = () -> "principal@example.com";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("principal@example.com", result.get());
        }

        @Test
        @DisplayName("should extract email from remote user")
        void shouldExtractEmailFromRemoteUser() {
            request.setRemoteUser("remote@example.com");

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("remote@example.com", result.get());
        }

        @Test
        @DisplayName("should skip non-email principal")
        void shouldSkipNonEmailPrincipal() {
            final Principal principal = () -> "admin";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should skip non-email remote user")
        void shouldSkipNonEmailRemoteUser() {
            request.setRemoteUser("admin");

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return empty when request is null")
        void shouldReturnEmptyWhenRequestIsNull() {
            final Optional<String> result = resolver.resolveUserId(null);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("email validation")
    class EmailValidation {

        @Test
        @DisplayName("should accept valid email format")
        void shouldAcceptValidEmailFormat() {
            final Principal principal = () -> "valid.email@example.com";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("valid.email@example.com", result.get());
        }

        @Test
        @DisplayName("should accept email with subdomain")
        void shouldAcceptEmailWithSubdomain() {
            final Principal principal = () -> "user@mail.example.com";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("user@mail.example.com", result.get());
        }

        @Test
        @DisplayName("should reject email without @ symbol")
        void shouldRejectEmailWithoutAtSymbol() {
            final Principal principal = () -> "userexample.com";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should reject email without dot in domain")
        void shouldRejectEmailWithoutDotInDomain() {
            final Principal principal = () -> "user@example";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should reject blank email")
        void shouldRejectBlankEmail() {
            final Principal principal = () -> "   ";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("priority order")
    class PriorityOrder {

        @Test
        @DisplayName("should prefer security context over request principal")
        void shouldPreferSecurityContextOverRequestPrincipal() {
            final UserDetails userDetails = User.builder()
                    .username("security@example.com")
                    .password("password")
                    .authorities(Collections.emptyList())
                    .build();
            setAuthentication(userDetails);

            final Principal principal = () -> "request@example.com";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("security@example.com", result.get());
        }

        @Test
        @DisplayName("should fallback to request principal when security context empty")
        void shouldFallbackToRequestPrincipal() {
            final Principal principal = () -> "request@example.com";
            request.setUserPrincipal(principal);

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("request@example.com", result.get());
        }

        @Test
        @DisplayName("should prefer request principal over remote user")
        void shouldPreferRequestPrincipalOverRemoteUser() {
            final Principal principal = () -> "principal@example.com";
            request.setUserPrincipal(principal);
            request.setRemoteUser("remote@example.com");

            final Optional<String> result = resolver.resolveUserId(request);

            assertTrue(result.isPresent());
            assertEquals("principal@example.com", result.get());
        }
    }
}
