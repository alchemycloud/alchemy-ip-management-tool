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
package cloud.alchemy.ip.integration;

import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.repository.IpAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for IP address storage functionality.
 * Uses a real HTTP server to ensure AOP aspects are properly triggered.
 */
@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@DisplayName("IP Address Storage Integration Tests")
class IpAddressStorageIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private IpAddressRepository repository;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("should store IP address on annotated endpoint")
    void shouldStoreIpAddressOnAnnotatedEndpoint() {
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Forwarded-For", "203.0.113.195")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("OK");

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());

        final IpAddressRecord record = records.getFirst();
        assertEquals("203.0.113.195", record.getIpAddress());
        assertNotNull(record.getCreatedAt());
    }

    @Test
    @DisplayName("should store full metadata when configured")
    void shouldStoreFullMetadataWhenConfigured() {
        webTestClient.get()
                .uri("/test/full-metadata")
                .header("X-Forwarded-For", "198.51.100.178")
                .header("User-Agent", "Mozilla/5.0 Integration Test")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());

        final IpAddressRecord record = records.getFirst();
        assertEquals("198.51.100.178", record.getIpAddress());
        assertEquals("Mozilla/5.0 Integration Test", record.getUserAgent());
        assertEquals("/test/full-metadata", record.getRequestPath());
        assertEquals("GET", record.getHttpMethod());
    }

    @Test
    @DisplayName("should store tag when specified")
    void shouldStoreTagWhenSpecified() {
        webTestClient.get()
                .uri("/test/tagged")
                .header("X-Forwarded-For", "192.0.2.1")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());

        final IpAddressRecord record = records.getFirst();
        assertEquals("192.0.2.1", record.getIpAddress());
        assertEquals("api-access", record.getTag());
    }

    @Test
    @DisplayName("should store IP asynchronously")
    void shouldStoreIpAsynchronously() {
        webTestClient.get()
                .uri("/test/async")
                .header("X-Forwarded-For", "192.0.2.50")
                .exchange()
                .expectStatus().isOk();

        // Wait for async storage to complete
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    final List<IpAddressRecord> records = repository.findAll();
                    assertEquals(1, records.size());
                    assertEquals("192.0.2.50", records.getFirst().getIpAddress());
                });
    }

    @Test
    @DisplayName("should store POST request method")
    void shouldStorePostRequestMethod() {
        webTestClient.post()
                .uri("/test/post")
                .header("X-Forwarded-For", "192.0.2.100")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());

        final IpAddressRecord record = records.getFirst();
        assertEquals("192.0.2.100", record.getIpAddress());
        assertEquals("POST", record.getHttpMethod());
    }

    @Test
    @DisplayName("should not store IP on non-annotated endpoint")
    void shouldNotStoreIpOnNonAnnotatedEndpoint() {
        webTestClient.get()
                .uri("/test/no-store")
                .header("X-Forwarded-For", "192.0.2.200")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Not stored");

        final List<IpAddressRecord> records = repository.findAll();
        assertTrue(records.isEmpty());
    }

    @Test
    @DisplayName("should use remote address when no proxy headers present")
    void shouldUseRemoteAddressWhenNoProxyHeaders() {
        webTestClient.get()
                .uri("/test/basic")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());

        // WebTestClient uses 127.0.0.1 as remote address
        final IpAddressRecord record = records.getFirst();
        assertEquals("127.0.0.1", record.getIpAddress());
    }

    @Test
    @DisplayName("should skip private IPs and use public IP from X-Forwarded-For")
    void shouldSkipPrivateIpsInXForwardedFor() {
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Forwarded-For", "10.0.0.1, 192.168.1.1, 203.0.113.50")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());
        assertEquals("203.0.113.50", records.getFirst().getIpAddress());
    }

    @Test
    @DisplayName("should prefer CF-Connecting-IP over X-Forwarded-For")
    void shouldPreferCfConnectingIpHeader() {
        webTestClient.get()
                .uri("/test/basic")
                .header("CF-Connecting-IP", "203.0.113.100")
                .header("X-Forwarded-For", "198.51.100.50")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());
        assertEquals("203.0.113.100", records.getFirst().getIpAddress());
    }

    @Test
    @DisplayName("should not store duplicate IP for same anonymous user")
    void shouldNotStoreDuplicateIpForAnonymousUser() {
        // First request
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Forwarded-For", "203.0.113.195")
                .exchange()
                .expectStatus().isOk();

        // Second request with same IP (anonymous user)
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Forwarded-For", "203.0.113.195")
                .exchange()
                .expectStatus().isOk();

        // Should only have one record
        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());
    }

    @Test
    @DisplayName("should store different IPs for anonymous user")
    void shouldStoreDifferentIpsForAnonymousUser() {
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Forwarded-For", "203.0.113.195")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/test/basic")
                .header("X-Forwarded-For", "198.51.100.50")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(2, records.size());
    }

    @Test
    @DisplayName("should handle IPv6 addresses")
    void shouldHandleIpv6Addresses() {
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Real-IP", "2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());
        assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", records.getFirst().getIpAddress());
    }

    @Test
    @DisplayName("should normalize IPv6 loopback address")
    void shouldNormalizeIpv6Loopback() {
        webTestClient.get()
                .uri("/test/basic")
                .header("X-Real-IP", "0:0:0:0:0:0:0:1")
                .exchange()
                .expectStatus().isOk();

        final List<IpAddressRecord> records = repository.findAll();
        assertEquals(1, records.size());
        assertEquals("::1", records.getFirst().getIpAddress());
    }
}
