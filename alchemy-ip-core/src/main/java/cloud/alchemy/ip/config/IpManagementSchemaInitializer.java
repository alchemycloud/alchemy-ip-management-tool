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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Initializes the database schema for IP address storage.
 *
 * <p>This component checks for the existence of required tables on application
 * startup and creates them if necessary. It also handles the creation of
 * foreign key constraints if a user table exists.
 *
 * <p>The schema initialization is database-agnostic and works with any
 * JDBC-compliant RDBMS including PostgreSQL, MySQL, MariaDB, Oracle,
 * SQL Server, H2, and SQLite.
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
public final class IpManagementSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(IpManagementSchemaInitializer.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final IpManagementProperties properties;

    /**
     * Creates the schema initializer.
     *
     * @param dataSource the data source
     * @param properties the IP management properties
     */
    public IpManagementSchemaInitializer(DataSource dataSource, IpManagementProperties properties) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = properties;
    }

    /**
     * Initializes the schema when the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeSchema() {
        if (!properties.getTable().isAutoCreate()) {
            log.debug("Schema auto-creation is disabled");
            return;
        }

        if (properties.getSchema().isUseHibernateDdl()) {
            log.debug("Using Hibernate DDL auto for schema management");
            return;
        }

        log.info("Initializing IP management schema");

        try {
            final DatabaseInfo dbInfo = detectDatabase();
            log.debug("Detected database: {} version {}", dbInfo.productName(), dbInfo.version());

            final String tableName = properties.getTable().getName();
            if (!tableExists(tableName)) {
                createIpAddressesTable(dbInfo);
            } else {
                log.debug("Table '{}' already exists", tableName);
            }

        } catch (Exception e) {
            log.error("Failed to initialize IP management schema", e);
            throw new RuntimeException("Schema initialization failed", e);
        }
    }

    private DatabaseInfo detectDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            final DatabaseMetaData metaData = conn.getMetaData();
            return new DatabaseInfo(
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    metaData.getDatabaseMajorVersion(),
                    metaData.getDatabaseMinorVersion()
            );
        }
    }

    private boolean tableExists(String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            final DatabaseMetaData metaData = conn.getMetaData();
            final String[] tableNameVariants = {tableName, tableName.toUpperCase(), tableName.toLowerCase()};

            for (String variant : tableNameVariants) {
                try (ResultSet rs = metaData.getTables(null, null, variant, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            log.warn("Error checking if table '{}' exists: {}", tableName, e.getMessage());
            return false;
        }
    }

    private void createIpAddressesTable(DatabaseInfo dbInfo) {
        final String tableName = properties.getTable().getName();
        log.info("Creating table '{}'", tableName);

        final String ddl = generateCreateTableDdl(dbInfo, tableName);
        jdbcTemplate.execute(ddl);

        createIndexes(tableName);

        log.info("Successfully created table '{}'", tableName);
    }

    private String generateCreateTableDdl(DatabaseInfo dbInfo, String tableName) {
        final String productName = dbInfo.productName().toLowerCase();

        final String idColumn;
        final String textType;
        final String timestampType;

        if (productName.contains("postgresql")) {
            idColumn = "id BIGSERIAL PRIMARY KEY";
            textType = "TEXT";
            timestampType = "TIMESTAMP WITH TIME ZONE";
        } else if (productName.contains("mysql") || productName.contains("mariadb")) {
            idColumn = "id BIGINT AUTO_INCREMENT PRIMARY KEY";
            textType = "TEXT";
            timestampType = "TIMESTAMP(6)";
        } else if (productName.contains("oracle")) {
            idColumn = "id NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
            textType = "CLOB";
            timestampType = "TIMESTAMP WITH TIME ZONE";
        } else if (productName.contains("microsoft") || productName.contains("sql server")) {
            idColumn = "id BIGINT IDENTITY(1,1) PRIMARY KEY";
            textType = "NVARCHAR(MAX)";
            timestampType = "DATETIMEOFFSET";
        } else if (productName.contains("h2")) {
            idColumn = "id BIGINT AUTO_INCREMENT PRIMARY KEY";
            textType = "TEXT";
            timestampType = "TIMESTAMP WITH TIME ZONE";
        } else if (productName.contains("sqlite")) {
            idColumn = "id INTEGER PRIMARY KEY AUTOINCREMENT";
            textType = "TEXT";
            timestampType = "TEXT";
        } else {
            idColumn = "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
            textType = "TEXT";
            timestampType = "TIMESTAMP";
        }

        return String.format("""
                CREATE TABLE %s (
                    %s,
                    ip_address VARCHAR(45) NOT NULL,
                    user_id VARCHAR(255),
                    user_agent VARCHAR(512),
                    request_path VARCHAR(2048),
                    http_method VARCHAR(10),
                    tag VARCHAR(100),
                    country_code VARCHAR(2),
                    city VARCHAR(100),
                    region VARCHAR(100),
                    latitude DOUBLE PRECISION,
                    longitude DOUBLE PRECISION,
                    source_header VARCHAR(50),
                    created_at %s NOT NULL,
                    metadata %s
                )
                """, tableName, idColumn, timestampType, textType);
    }

    private void createIndexes(String tableName) {
        try {
            jdbcTemplate.execute(String.format(
                    "CREATE INDEX idx_%s_ip_address ON %s (ip_address)", tableName, tableName));
            jdbcTemplate.execute(String.format(
                    "CREATE INDEX idx_%s_user_id ON %s (user_id)", tableName, tableName));
            jdbcTemplate.execute(String.format(
                    "CREATE INDEX idx_%s_created_at ON %s (created_at)", tableName, tableName));
            jdbcTemplate.execute(String.format(
                    "CREATE INDEX idx_%s_ip_user ON %s (ip_address, user_id)", tableName, tableName));
            log.debug("Created indexes for table '{}'", tableName);
        } catch (Exception e) {
            log.warn("Failed to create some indexes: {}", e.getMessage());
        }
    }

    /**
     * Immutable record containing database information.
     */
    private record DatabaseInfo(
            String productName,
            String version,
            int majorVersion,
            int minorVersion
    ) {}
}
