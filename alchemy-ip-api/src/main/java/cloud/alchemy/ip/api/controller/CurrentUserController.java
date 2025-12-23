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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for current user information.
 * Only active when Spring Security is on the classpath.
 */
@RestController
@RequestMapping("${alchemy.ip.api.base-path:/api/alchemy-ip}")
@ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
public class CurrentUserController {

    /**
     * Get current authenticated user information.
     * Used by the Angular dashboard to check authentication status.
     */
    @GetMapping("/current-user")
    public Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || isAnonymous(auth)) {
            return Map.of("authenticated", false);
        }

        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Map.of(
                "authenticated", true,
                "username", auth.getName(),
                "authorities", authorities
        );
    }

    private boolean isAnonymous(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"));
    }
}
