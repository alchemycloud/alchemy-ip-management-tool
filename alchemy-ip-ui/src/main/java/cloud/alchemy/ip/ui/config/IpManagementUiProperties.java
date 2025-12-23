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
package cloud.alchemy.ip.ui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the IP Management UI module.
 */
@ConfigurationProperties(prefix = "alchemy.ip.ui")
public class IpManagementUiProperties {

    /**
     * Whether the UI module is enabled.
     */
    private boolean enabled = true;

    /**
     * Base path for serving the dashboard UI.
     */
    private String basePath = "/ip-management";

    /**
     * Cache period for static resources in seconds.
     */
    private int cacheSeconds = 3600;

    /**
     * Dashboard title displayed in the UI.
     */
    private String title = "IP Management Dashboard";

    /**
     * Theme: 'default', 'dark', 'cosmic', 'corporate'.
     */
    private String theme = "default";

    /**
     * Refresh interval for dashboard in seconds.
     */
    private int refreshInterval = 30;

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

    public int getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds(int cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
