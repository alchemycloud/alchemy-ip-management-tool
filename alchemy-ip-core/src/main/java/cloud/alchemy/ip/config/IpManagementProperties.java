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
package cloud.alchemy.ip.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the Alchemy IP Management Tool.
 *
 * <p>These properties can be configured in {@code application.properties} or
 * {@code application.yml} with the prefix {@code alchemy.ip}.
 *
 * <p>Example configuration:
 * <pre>{@code
 * alchemy.ip.enabled=true
 * alchemy.ip.async.enabled=true
 * alchemy.ip.async.core-pool-size=2
 * alchemy.ip.async.max-pool-size=10
 * alchemy.ip.table.name=ip_addresses
 * alchemy.ip.table.auto-create=true
 * alchemy.ip.trusted-proxies[0]=10.0.0.0/8
 * alchemy.ip.trusted-proxies[1]=172.16.0.0/12
 * }</pre>
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "alchemy.ip")
public class IpManagementProperties {

    /**
     * Whether IP management is enabled. Defaults to true.
     */
    private boolean enabled = true;

    /**
     * Whether to trust all proxy headers. If false, only headers from
     * trusted proxies will be considered.
     */
    private boolean trustAllProxies = true;

    /**
     * List of trusted proxy IP addresses or CIDR ranges.
     */
    private List<String> trustedProxies = new ArrayList<>();

    /**
     * Async processing configuration.
     */
    private Async async = new Async();

    /**
     * Table configuration.
     */
    private Table table = new Table();

    /**
     * Schema configuration.
     */
    private Schema schema = new Schema();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isTrustAllProxies() {
        return trustAllProxies;
    }

    public void setTrustAllProxies(boolean trustAllProxies) {
        this.trustAllProxies = trustAllProxies;
    }

    public List<String> getTrustedProxies() {
        return trustedProxies;
    }

    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies;
    }

    public Async getAsync() {
        return async;
    }

    public void setAsync(Async async) {
        this.async = async;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * Async processing configuration.
     */
    public static class Async {

        /**
         * Whether async processing is enabled. Defaults to true.
         */
        private boolean enabled = true;

        /**
         * Core pool size for the async executor.
         */
        private int corePoolSize = 2;

        /**
         * Maximum pool size for the async executor.
         */
        private int maxPoolSize = 10;

        /**
         * Queue capacity for the async executor.
         */
        private int queueCapacity = 100;

        /**
         * Thread name prefix for async threads.
         */
        private String threadNamePrefix = "ip-storage-";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    /**
     * Table configuration.
     */
    public static class Table {

        /**
         * The name of the IP addresses table. Defaults to "ip_addresses".
         */
        private String name = "ip_addresses";

        /**
         * Whether to auto-create the table if it doesn't exist.
         */
        private boolean autoCreate = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isAutoCreate() {
            return autoCreate;
        }

        public void setAutoCreate(boolean autoCreate) {
            this.autoCreate = autoCreate;
        }
    }

    /**
     * Schema configuration.
     */
    public static class Schema {

        /**
         * Whether to use Hibernate DDL auto for schema management.
         * If false, custom schema initialization will be used.
         */
        private boolean useHibernateDdl = true;

        public boolean isUseHibernateDdl() {
            return useHibernateDdl;
        }

        public void setUseHibernateDdl(boolean useHibernateDdl) {
            this.useHibernateDdl = useHibernateDdl;
        }
    }
}
