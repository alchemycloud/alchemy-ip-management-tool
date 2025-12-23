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
package cloud.alchemy.ip.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

/**
 * Auto-configuration for securing the IP Management API endpoints.
 * Only active when Spring Security is on the classpath.
 *
 * <p>This configuration integrates with the host application's existing
 * authentication mechanism. It does not provide its own login form or
 * user management.
 *
 * <p>Configuration properties:
 * <ul>
 *   <li>{@code alchemy.ip.api.security.enabled} - Enable/disable security (default: true)</li>
 *   <li>{@code alchemy.ip.api.security.allowed-roles} - Roles allowed to access the API (default: ADMIN)</li>
 * </ul>
 */
@AutoConfiguration(after = IpManagementApiAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.security.config.annotation.web.builders.HttpSecurity")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "alchemy.ip.api.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IpManagementApiProperties.class)
public class IpManagementApiSecurityAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(IpManagementApiSecurityAutoConfiguration.class);

    private final IpManagementApiProperties properties;

    public IpManagementApiSecurityAutoConfiguration(IpManagementApiProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a security filter chain specifically for the IP Management API endpoints.
     * This filter chain has a higher precedence (lower order number) than the default.
     *
     * <p>Security behavior:
     * <ul>
     *   <li>Requires authentication for all API endpoints</li>
     *   <li>If allowed-roles is configured, requires one of the specified roles</li>
     *   <li>CSRF is disabled for API endpoints (assuming token-based auth)</li>
     * </ul>
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public SecurityFilterChain ipManagementApiSecurityFilterChain(HttpSecurity http) throws Exception {
        String basePath = properties.getBasePath();
        List<String> allowedRoles = properties.getSecurity().getAllowedRoles();

        logger.info("Configuring security for IP Management API at path: {}", basePath);

        http
            .securityMatcher(new AntPathRequestMatcher(basePath + "/**"))
            .authorizeHttpRequests(auth -> {
                if (allowedRoles != null && !allowedRoles.isEmpty()) {
                    String[] roles = allowedRoles.toArray(new String[0]);
                    logger.info("Restricting access to roles: {}", allowedRoles);
                    auth.anyRequest().hasAnyRole(roles);
                } else {
                    logger.info("Requiring authentication for API access");
                    auth.anyRequest().authenticated();
                }
            })
            // Disable CSRF for API endpoints (common for REST APIs with token auth)
            .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
