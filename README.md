# Alchemy IP Management Tool

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)

A Spring Boot starter library for automatically capturing and storing client IP addresses from incoming HTTP requests. Designed for multi-cloud deployments with support for various reverse proxy configurations.

## Features

- **Simple Annotation-Based Usage**: Just add `@StoreIPAddress` to your controller methods
- **Multi-Cloud Support**: Automatically extracts real client IPs from various cloud providers (AWS, GCP, Azure, Cloudflare, etc.)
- **Database Agnostic**: Works with any RDBMS supported by Hibernate (PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, H2, SQLite)
- **Auto Schema Management**: Automatically creates required tables on startup
- **Fully Customizable**: Override any component (IP extraction, user ID resolution, persistence)
- **Async Processing**: Non-blocking IP storage with configurable thread pool
- **Extensible Entity**: Extend the base entity to add custom columns
- **Optional Dashboard UI**: Built-in Angular dashboard for visualizing IP data

## Module Structure

The project consists of three modules:

| Module | Artifact ID | Description |
|--------|-------------|-------------|
| **Core** | `alchemy-ip-core` | Core library with annotations, repository, and services |
| **API** | `alchemy-ip-api` | REST API for accessing stored IP data |
| **UI** | `alchemy-ip-ui` | Angular dashboard for visualization |

## Installation

### Core Library Only

For just the IP capturing functionality:

```xml
<dependency>
    <groupId>cloud.alchemy</groupId>
    <artifactId>alchemy-ip-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### With REST API

To expose IP data via REST endpoints:

```xml
<dependency>
    <groupId>cloud.alchemy</groupId>
    <artifactId>alchemy-ip-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### With Dashboard UI

For the full dashboard experience (includes both API and Core):

```xml
<dependency>
    <groupId>cloud.alchemy</groupId>
    <artifactId>alchemy-ip-ui</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### 1. Add the annotation to your controller methods

```java
@RestController
@RequestMapping("/api")
public class UserController {

    @StoreIPAddress
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }

    @StoreIPAddress(storeUserAgent = true, tag = "login")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
```

### 2. Configure your database

The library will automatically create the `ip_addresses` table on startup:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
  jpa:
    hibernate:
      ddl-auto: update
```

### 3. Access the Dashboard (optional)

If you included the UI module, access the dashboard at:

```
http://localhost:8080/ip-management
```

---

## Dashboard UI

The optional dashboard provides a visual interface for exploring IP data.

### Features

- **Dashboard**: Summary statistics, recent activity, top IPs, and geographic distribution
- **IP Records**: Searchable, filterable, paginated table of all records with detail view
- **Statistics**: Geographic charts, frequent IP analysis, and lookup tools

### Screenshots

**Dashboard View:**
- Total records, unique IPs, unique users statistics
- Timeline chart showing records over time
- Top IP addresses and countries
- Recent activity feed

**IP Records View:**
- Full-featured data table with sorting and pagination
- Advanced filters (IP, user, country, method, tag, date range)
- Record detail modal with all fields

**Statistics View:**
- Geographic distribution by country and city
- Frequent IP addresses analysis
- IP and user lookup tools

### Configuration

```yaml
alchemy:
  ip:
    ui:
      enabled: true                    # Enable/disable the dashboard
      base-path: /ip-management        # URL path for the dashboard
      cache-seconds: 3600              # Static resource cache duration
      title: "IP Management Dashboard" # Browser title
      theme: default                   # Theme (default, dark, cosmic, corporate)
      refresh-interval: 30             # Auto-refresh interval in seconds
```

### Security

The dashboard integrates with your application's existing Spring Security configuration. **It does not provide its own login form.**

#### Configuring Access Control

```yaml
alchemy:
  ip:
    api:
      security:
        enabled: true                  # Enable security for API endpoints
        allowed-roles:                 # Roles allowed to access the dashboard
          - ADMIN
          - IP_VIEWER
```

#### How Security Works

1. API endpoints are secured via Spring Security
2. If `allowed-roles` is set, only users with those roles can access
3. The Angular dashboard checks `/api/alchemy-ip/current-user` to verify authentication
4. Unauthenticated users should be redirected to your app's login page by Spring Security

#### Example Security Configuration

If your app doesn't have Spring Security configured, add a basic setup:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/alchemy-ip/**").hasRole("ADMIN")
                .requestMatchers("/ip-management/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form.defaultSuccessUrl("/ip-management"))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
```

---

## REST API Reference

The API module exposes the following endpoints (base path: `/api/alchemy-ip`):

### Records Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/records` | List all records (paginated) |
| GET | `/records/{id}` | Get record by ID |
| GET | `/records/ip/{ipAddress}` | Find by IP address |
| GET | `/records/user/{userId}` | Find by user ID |
| GET | `/records/tag/{tag}` | Find by tag |
| GET | `/records/country/{countryCode}` | Find by country |
| GET | `/records/date-range?startDate=&endDate=` | Find by date range |
| POST | `/records/search` | Advanced search with criteria |
| DELETE | `/records/before/{timestamp}` | Delete old records |

### Statistics Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/stats/summary` | Dashboard summary statistics |
| GET | `/stats/timeline?days=30` | Records over time |
| GET | `/stats/geographic` | Geographic distribution |
| GET | `/stats/frequent?threshold=2` | Frequent IP addresses |
| GET | `/stats/ip/{ipAddress}/count` | Count by IP |
| GET | `/stats/user/{userId}/count` | Count by user |
| GET | `/stats/user/{userId}/distinct-ips` | Distinct IPs for user |
| GET | `/stats/ip/{ipAddress}/distinct-users` | Distinct users for IP |

### Authentication Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/current-user` | Get current authenticated user info |

### Query Parameters

Common parameters for paginated endpoints:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | 0 | Page number (0-indexed) |
| `size` | 20 | Page size (max 100) |
| `sortBy` | createdAt | Sort field |
| `direction` | DESC | Sort direction (ASC/DESC) |

### Search Criteria (POST /records/search)

```json
{
  "ipAddress": "192.168.1.1",
  "userId": "user@example.com",
  "tag": "login",
  "countryCode": "US",
  "httpMethod": "POST",
  "requestPathPattern": "/api/%",
  "startDate": "2024-01-01T00:00:00Z",
  "endDate": "2024-12-31T23:59:59Z"
}
```

### API Configuration

```yaml
alchemy:
  ip:
    api:
      enabled: true                    # Enable/disable the API
      base-path: /api/alchemy-ip       # API base path
      default-page-size: 20            # Default page size
      max-page-size: 100               # Maximum page size
      cors-enabled: false              # Enable CORS
      cors-allowed-origins:            # Allowed CORS origins
        - http://localhost:4200
```

---

## Core Configuration

All configuration properties use the `alchemy.ip` prefix:

```yaml
alchemy:
  ip:
    enabled: true                    # Enable/disable the library
    trust-all-proxies: true          # Trust all proxy headers
    trusted-proxies:                 # List of trusted proxy IPs
      - 10.0.0.0/8
      - 172.16.0.0/12

    async:
      enabled: true                  # Enable async IP storage
      core-pool-size: 2              # Thread pool core size
      max-pool-size: 10              # Thread pool max size
      queue-capacity: 100            # Task queue capacity

    table:
      name: ip_addresses             # Table name
      auto-create: true              # Auto-create table

    schema:
      use-hibernate-ddl: true        # Use Hibernate for DDL
```

## Annotation Options

The `@StoreIPAddress` annotation supports the following options:

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `storeUserAgent` | boolean | false | Store the User-Agent header |
| `storeRequestPath` | boolean | true | Store the request URI |
| `storeHttpMethod` | boolean | true | Store the HTTP method |
| `storeTimestamp` | boolean | true | Store the request timestamp |
| `storeGeoLocation` | boolean | false | Store geo-location data |
| `tag` | String | "" | Custom tag for categorization |
| `async` | boolean | true | Store asynchronously |
| `userIdExpression` | String | "" | SpEL expression for user ID |

### SpEL Expression for User ID

```java
@StoreIPAddress(userIdExpression = "#request.getHeader('X-User-Id')")
@GetMapping("/resource")
public Resource getResource() { ... }

@StoreIPAddress(userIdExpression = "@userService.getCurrentUserId()")
@PostMapping("/action")
public void performAction() { ... }
```

## Supported IP Headers

The library checks the following headers in order (first valid IP wins):

| Header | Provider/Use Case |
|--------|-------------------|
| `CF-Connecting-IP` | Cloudflare |
| `True-Client-IP` | Akamai, Cloudflare Enterprise |
| `Fastly-Client-IP` | Fastly CDN |
| `X-Azure-ClientIP` | Azure Front Door |
| `X-Appengine-User-IP` | Google App Engine |
| `X-Real-IP` | Nginx |
| `X-Forwarded-For` | Standard proxy header (AWS, GCP, etc.) |
| `X-Original-Forwarded-For` | AWS ALB behind CloudFront |
| `X-Client-IP` | Apache |
| `X-Cluster-Client-IP` | Rackspace, Riverbed |
| `Forwarded` | RFC 7239 standard |

## Customization

### Custom User ID Resolver

```java
@Component
@Primary
public class SecurityUserIdResolver implements UserIdResolver {

    @Override
    public Optional<String> resolveUserId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return Optional.of(auth.getName());
        }
        return Optional.empty();
    }
}
```

### Custom IP Extractor

```java
@Component
@Primary
public class CustomIpExtractor implements IpAddressExtractor {

    @Override
    public Optional<String> extractIpAddress(HttpServletRequest request) {
        String customHeader = request.getHeader("X-Custom-IP");
        if (customHeader != null) {
            return Optional.of(customHeader);
        }
        return Optional.ofNullable(request.getRemoteAddr());
    }
}
```

### Record Customizer

```java
@Component
public class GeoLocationCustomizer implements IpRecordCustomizer {

    private final GeoLocationService geoService;

    @Override
    public void customize(IpAddressRecord record, HttpServletRequest request) {
        GeoLocation loc = geoService.lookup(record.getIpAddress());
        if (loc != null) {
            record.setCountryCode(loc.getCountryCode());
            record.setCity(loc.getCity());
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
```

## Database Schema

The `ip_addresses` table has the following structure:

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT | Primary key (auto-increment) |
| `ip_address` | VARCHAR(45) | IPv4 or IPv6 address |
| `user_id` | VARCHAR(255) | User identifier |
| `user_agent` | VARCHAR(512) | Browser User-Agent |
| `request_path` | VARCHAR(2048) | Request URI |
| `http_method` | VARCHAR(10) | HTTP method |
| `tag` | VARCHAR(100) | Custom tag |
| `country_code` | VARCHAR(2) | ISO country code |
| `city` | VARCHAR(100) | City name |
| `region` | VARCHAR(100) | Region/state |
| `latitude` | DOUBLE | Latitude |
| `longitude` | DOUBLE | Longitude |
| `source_header` | VARCHAR(50) | Header IP was extracted from |
| `created_at` | TIMESTAMP | Record creation time |
| `metadata` | TEXT | Additional JSON metadata |

## Requirements

- Java 21+
- Spring Boot 3.4+
- Any RDBMS supported by Hibernate

## Building from Source

```bash
git clone https://github.com/alchemy-io/alchemy-ip-management-tool.git
cd alchemy-ip-management-tool
./mvnw clean install
```

### Build Options

```bash
# Build all modules
./mvnw clean install

# Build without frontend (faster Java-only build)
./mvnw clean install -P skip-frontend

# Build specific module
./mvnw clean install -pl alchemy-ip-core
```

## Tips & Best Practices

### Performance

1. **Use async storage** (default) to avoid blocking request processing
2. **Configure appropriate thread pool size** based on your traffic
3. **Set up database indexes** (auto-created by default)
4. **Use the cleanup endpoint** to purge old records periodically

### Security

1. **Restrict dashboard access** using `allowed-roles` configuration
2. **Don't expose the API publicly** without authentication
3. **Validate trusted proxies** in production environments

### Monitoring

1. **Use the dashboard** for quick insights
2. **Query the repository** for custom analytics
3. **Export data via API** for external analysis tools

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- [GitHub Issues](https://github.com/alchemy-io/alchemy-ip-management-tool/issues)
- [Documentation](https://github.com/alchemy-io/alchemy-ip-management-tool/wiki)
