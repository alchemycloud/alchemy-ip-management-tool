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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the IP Management API module.
 */
@ConfigurationProperties(prefix = "alchemy.ip.api")
public class IpManagementApiProperties {

    /**
     * Whether the API module is enabled.
     */
    private boolean enabled = true;

    /**
     * Base path for REST API endpoints.
     */
    private String basePath = "/api/alchemy-ip";

    /**
     * Default page size for paginated responses.
     */
    private int defaultPageSize = 20;

    /**
     * Maximum allowed page size.
     */
    private int maxPageSize = 100;

    /**
     * Enable CORS for API endpoints.
     */
    private boolean corsEnabled = false;

    /**
     * Allowed CORS origins (if corsEnabled).
     */
    private List<String> corsAllowedOrigins = new ArrayList<>(List.of("*"));

    /**
     * Security configuration.
     */
    private Security security = new Security();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public boolean isCorsEnabled() {
        return corsEnabled;
    }

    public void setCorsEnabled(boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    /**
     * Security sub-configuration.
     */
    public static class Security {

        /**
         * Whether security is enabled for API endpoints.
         */
        private boolean enabled = true;

        /**
         * Roles allowed to access the API.
         */
        private List<String> allowedRoles = new ArrayList<>(List.of("ADMIN"));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }
    }
}
