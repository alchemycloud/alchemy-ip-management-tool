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
package cloud.alchemy.ip.entity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity representing an IP address record stored in the database.
 *
 * <p>This entity maps to the {@code ip_addresses} table and stores
 * information about incoming request IP addresses along with metadata.
 *
 * <p>The entity is designed to be extensible. Users can extend this class
 * to add custom columns or override persistence behavior.
 *
 * <p>Note: Due to JPA requirements, this entity cannot be fully immutable.
 * Use the {@link Builder} to create instances with all fields set.
 *
 * <p>Example of extending this entity:
 * <pre>{@code
 * @Entity
 * @Table(name = "ip_addresses")
 * public class CustomIpAddressRecord extends IpAddressRecord {
 *
 *     @Column(name = "custom_field")
 *     private String customField;
 *
 *     // getters and setters
 * }
 * }</pre>
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see cloud.alchemy.ip.repository.IpAddressRepository
 */
@Entity
@Access(AccessType.FIELD)
@Table(
        name = "ip_addresses",
        indexes = {
                @Index(name = "idx_ip_address", columnList = "ip_address"),
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_ip_user", columnList = "ip_address, user_id")
        }
)
public class IpAddressRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_id", length = 255)
    private String userId;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "request_path", length = 2048)
    private String requestPath;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "tag", length = 100)
    private String tag;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "source_header", length = 50)
    private String sourceHeader;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Default constructor required by JPA.
     */
    protected IpAddressRecord() {
    }

    /**
     * Private constructor used by the Builder.
     */
    private IpAddressRecord(Builder builder) {
        this.ipAddress = builder.ipAddress;
        this.userId = builder.userId;
        this.userAgent = builder.userAgent;
        this.requestPath = builder.requestPath;
        this.httpMethod = builder.httpMethod;
        this.tag = builder.tag;
        this.countryCode = builder.countryCode;
        this.city = builder.city;
        this.region = builder.region;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.sourceHeader = builder.sourceHeader;
        this.createdAt = builder.createdAt;
        this.metadata = builder.metadata;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // Getters only - no setters for immutability (JPA uses field access)

    public Long getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getTag() {
        return tag;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getSourceHeader() {
        return sourceHeader;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getMetadata() {
        return metadata;
    }

    /**
     * Creates a new builder initialized with this record's values.
     * Useful for creating modified copies.
     *
     * @return a new Builder pre-populated with this record's values
     */
    public Builder toBuilder() {
        return new Builder()
                .ipAddress(this.ipAddress)
                .userId(this.userId)
                .userAgent(this.userAgent)
                .requestPath(this.requestPath)
                .httpMethod(this.httpMethod)
                .tag(this.tag)
                .countryCode(this.countryCode)
                .city(this.city)
                .region(this.region)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .sourceHeader(this.sourceHeader)
                .createdAt(this.createdAt)
                .metadata(this.metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpAddressRecord that = (IpAddressRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IpAddressRecord{" +
                "id=" + id +
                ", ipAddress='" + ipAddress + '\'' +
                ", userId='" + userId + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Creates a new Builder for IpAddressRecord.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for creating IpAddressRecord instances.
     * This is the preferred way to create new records.
     */
    public static final class Builder {
        private String ipAddress;
        private String userId;
        private String userAgent;
        private String requestPath;
        private String httpMethod;
        private String tag;
        private String countryCode;
        private String city;
        private String region;
        private Double latitude;
        private Double longitude;
        private String sourceHeader;
        private Instant createdAt;
        private String metadata;

        private Builder() {
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder region(String region) {
            this.region = region;
            return this;
        }

        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder sourceHeader(String sourceHeader) {
            this.sourceHeader = sourceHeader;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Builds a new IpAddressRecord instance.
         *
         * @return a new IpAddressRecord
         * @throws IllegalStateException if ipAddress is null or blank
         */
        public IpAddressRecord build() {
            if (ipAddress == null || ipAddress.isBlank()) {
                throw new IllegalStateException("ipAddress is required");
            }
            return new IpAddressRecord(this);
        }
    }
}
