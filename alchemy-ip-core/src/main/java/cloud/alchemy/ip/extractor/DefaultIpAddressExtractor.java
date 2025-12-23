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
package cloud.alchemy.ip.extractor;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link IpAddressExtractor} that supports
 * multiple cloud providers and proxy configurations.
 *
 * <p>This extractor checks various HTTP headers commonly used by different
 * cloud providers and reverse proxies to forward the original client IP address:
 *
 * <ul>
 *   <li><b>CF-Connecting-IP</b> - Cloudflare</li>
 *   <li><b>True-Client-IP</b> - Akamai, Cloudflare Enterprise</li>
 *   <li><b>X-Real-IP</b> - Nginx</li>
 *   <li><b>X-Forwarded-For</b> - Standard proxy header (AWS, GCP, Azure, etc.)</li>
 *   <li><b>X-Client-IP</b> - Apache</li>
 *   <li><b>X-Cluster-Client-IP</b> - Rackspace, Riverbed</li>
 *   <li><b>Forwarded</b> - RFC 7239 standard header</li>
 *   <li><b>X-Original-Forwarded-For</b> - AWS ALB when behind CloudFront</li>
 *   <li><b>X-Azure-ClientIP</b> - Azure Front Door</li>
 *   <li><b>X-Appengine-User-IP</b> - Google App Engine</li>
 *   <li><b>Fastly-Client-IP</b> - Fastly CDN</li>
 * </ul>
 *
 * <p>The headers are checked in order of specificity and reliability.
 * If no proxy headers are found, the remote address from the request is used.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
public final class DefaultIpAddressExtractor implements IpAddressExtractor {

    private static final Logger log = LoggerFactory.getLogger(DefaultIpAddressExtractor.class);

    /**
     * Headers to check for client IP, ordered by specificity and reliability.
     */
    private static final List<String> IP_HEADERS = List.of(
            "CF-Connecting-IP",
            "True-Client-IP",
            "Fastly-Client-IP",
            "X-Azure-ClientIP",
            "X-Appengine-User-IP",
            "X-Real-IP",
            "X-Forwarded-For",
            "X-Original-Forwarded-For",
            "X-Client-IP",
            "X-Cluster-Client-IP",
            "Forwarded"
    );

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    );

    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
            "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,7}:$|" +
            "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$|" +
            "^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$|" +
            "^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$|" +
            "^[0-9a-fA-F]{1,4}:(:[0-9a-fA-F]{1,4}){1,6}$|" +
            "^:((:[0-9a-fA-F]{1,4}){1,7}|:)$"
    );

    /**
     * RFC 1918 private IPv4 ranges as CIDR blocks.
     * Each entry is {network address bytes, prefix length}.
     */
    private static final List<CidrBlock> PRIVATE_IPV4_RANGES = List.of(
            new CidrBlock(new byte[]{10, 0, 0, 0}, 8),           // 10.0.0.0/8
            new CidrBlock(new byte[]{(byte) 172, 16, 0, 0}, 12), // 172.16.0.0/12
            new CidrBlock(new byte[]{(byte) 192, (byte) 168, 0, 0}, 16), // 192.168.0.0/16
            new CidrBlock(new byte[]{127, 0, 0, 0}, 8),          // 127.0.0.0/8 (loopback)
            new CidrBlock(new byte[]{(byte) 169, (byte) 254, 0, 0}, 16), // 169.254.0.0/16 (link-local)
            new CidrBlock(new byte[]{0, 0, 0, 0}, 8)             // 0.0.0.0/8 (current network)
    );

    private final boolean trustAllProxies;
    private final List<String> trustedProxies;

    /**
     * Creates a new extractor that trusts all proxy headers.
     */
    public DefaultIpAddressExtractor() {
        this(true, List.of());
    }

    /**
     * Creates a new extractor with custom proxy trust settings.
     *
     * @param trustAllProxies whether to trust all proxy headers
     * @param trustedProxies  list of trusted proxy IP addresses (defensively copied)
     */
    public DefaultIpAddressExtractor(boolean trustAllProxies, List<String> trustedProxies) {
        this.trustAllProxies = trustAllProxies;
        this.trustedProxies = trustedProxies != null ? List.copyOf(trustedProxies) : List.of();
    }

    @Override
    public Optional<String> extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            log.warn("Cannot extract IP address from null request");
            return Optional.empty();
        }

        for (String header : IP_HEADERS) {
            final String headerValue = request.getHeader(header);
            if (isValidHeaderValue(headerValue)) {
                final Optional<String> ip = parseIpFromHeader(header, headerValue);
                if (ip.isPresent()) {
                    log.debug("Extracted IP address '{}' from header '{}'", ip.get(), header);
                    return ip;
                }
            }
        }

        final String remoteAddr = request.getRemoteAddr();
        if (isValidIpAddress(remoteAddr)) {
            log.debug("Using remote address as IP: {}", remoteAddr);
            return Optional.of(normalizeIpAddress(remoteAddr));
        }

        log.warn("Could not extract IP address from request");
        return Optional.empty();
    }

    /**
     * Returns whether this extractor trusts all proxies.
     *
     * @return true if all proxies are trusted
     */
    public boolean isTrustAllProxies() {
        return trustAllProxies;
    }

    /**
     * Returns an immutable list of trusted proxy addresses.
     *
     * @return immutable list of trusted proxies
     */
    public List<String> getTrustedProxies() {
        return trustedProxies; // Already immutable from constructor
    }

    private Optional<String> parseIpFromHeader(String headerName, String headerValue) {
        if ("Forwarded".equalsIgnoreCase(headerName)) {
            return parseForwardedHeader(headerValue);
        }

        if ("X-Forwarded-For".equalsIgnoreCase(headerName) ||
            "X-Original-Forwarded-For".equalsIgnoreCase(headerName)) {
            return parseXForwardedForHeader(headerValue);
        }

        final String trimmed = headerValue.trim();
        if (isValidIpAddress(trimmed)) {
            return Optional.of(normalizeIpAddress(trimmed));
        }

        return Optional.empty();
    }

    private Optional<String> parseForwardedHeader(String headerValue) {
        final String[] parts = headerValue.split(";");
        for (String part : parts) {
            final String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith("for=")) {
                String forValue = trimmed.substring(4).trim();
                if (forValue.startsWith("\"") && forValue.endsWith("\"")) {
                    forValue = forValue.substring(1, forValue.length() - 1);
                }
                if (forValue.startsWith("[")) {
                    final int bracketEnd = forValue.indexOf(']');
                    if (bracketEnd > 0) {
                        forValue = forValue.substring(1, bracketEnd);
                    }
                }
                if (forValue.contains(":") && !forValue.contains("[")) {
                    forValue = forValue.split(":")[0];
                }
                if (isValidIpAddress(forValue)) {
                    return Optional.of(normalizeIpAddress(forValue));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> parseXForwardedForHeader(String headerValue) {
        final String[] ips = headerValue.split(",");

        // First pass: find the first public (non-private) IP
        for (String ip : ips) {
            String trimmed = ip.trim();
            if (trimmed.contains(":") && !trimmed.contains("[")) {
                trimmed = trimmed.split(":")[0];
            }
            if (isValidIpAddress(trimmed)) {
                final String normalized = normalizeIpAddress(trimmed);
                if (!isPrivateIpAddress(normalized)) {
                    return Optional.of(normalized);
                }
            }
        }

        // Second pass: if no public IP found and we trust all proxies, return first valid IP
        if (trustAllProxies) {
            for (String ip : ips) {
                String trimmed = ip.trim();
                if (trimmed.contains(":") && !trimmed.contains("[")) {
                    trimmed = trimmed.split(":")[0];
                }
                if (isValidIpAddress(trimmed)) {
                    return Optional.of(normalizeIpAddress(trimmed));
                }
            }
        }

        return Optional.empty();
    }

    private boolean isValidHeaderValue(String value) {
        return value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value.trim());
    }

    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        final String trimmed = ip.trim();
        return IPV4_PATTERN.matcher(trimmed).matches() ||
               IPV6_PATTERN.matcher(trimmed).matches() ||
               "0:0:0:0:0:0:0:1".equals(trimmed) ||
               "::1".equals(trimmed);
    }

    private boolean isPrivateIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        try {
            final InetAddress address = InetAddress.getByName(ip);

            // Use built-in checks for common cases
            if (address.isLoopbackAddress() || address.isLinkLocalAddress() ||
                address.isSiteLocalAddress() || address.isAnyLocalAddress()) {
                return true;
            }

            // Additional check for IPv4 private ranges using CIDR matching
            final byte[] addressBytes = address.getAddress();
            if (addressBytes.length == 4) { // IPv4
                for (CidrBlock cidr : PRIVATE_IPV4_RANGES) {
                    if (cidr.contains(addressBytes)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (UnknownHostException e) {
            log.debug("Could not parse IP address '{}': {}", ip, e.getMessage());
            return false;
        }
    }

    /**
     * Immutable record representing a CIDR block for IP range matching.
     */
    private record CidrBlock(byte[] network, int prefixLength) {

        boolean contains(byte[] address) {
            if (address.length != network.length) {
                return false;
            }

            final int fullBytes = prefixLength / 8;
            final int remainingBits = prefixLength % 8;

            // Check full bytes
            for (int i = 0; i < fullBytes; i++) {
                if (address[i] != network[i]) {
                    return false;
                }
            }

            // Check remaining bits if any
            if (remainingBits > 0 && fullBytes < network.length) {
                final int mask = (0xFF << (8 - remainingBits)) & 0xFF;
                if ((address[fullBytes] & mask) != (network[fullBytes] & mask)) {
                    return false;
                }
            }

            return true;
        }
    }

    private String normalizeIpAddress(String ip) {
        if (ip == null) {
            return null;
        }
        final String normalized = ip.trim();
        if ("0:0:0:0:0:0:0:1".equals(normalized)) {
            return "::1";
        }
        return normalized;
    }
}
