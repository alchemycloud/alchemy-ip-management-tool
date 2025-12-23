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

/**
 * Alchemy IP Management Tool - A Spring Boot starter for IP address management.
 *
 * <p>This library provides automatic capture and storage of client IP addresses
 * from incoming HTTP requests in Spring Boot applications.
 *
 * <h2>Quick Start</h2>
 *
 * <p>Add {@link cloud.alchemy.ip.annotation.StoreIPAddress @StoreIPAddress} to your
 * controller methods:
 *
 * <pre>{@code
 * @RestController
 * public class MyController {
 *
 *     @StoreIPAddress
 *     @GetMapping("/api/resource")
 *     public Resource getResource() {
 *         return resourceService.get();
 *     }
 * }
 * }</pre>
 *
 * <h2>Key Components</h2>
 *
 * <ul>
 *   <li>{@link cloud.alchemy.ip.annotation.StoreIPAddress} - Annotation to mark methods for IP storage</li>
 *   <li>{@link cloud.alchemy.ip.entity.IpAddressRecord} - JPA entity for IP address records</li>
 *   <li>{@link cloud.alchemy.ip.repository.IpAddressRepository} - Repository for querying IP records</li>
 *   <li>{@link cloud.alchemy.ip.extractor.IpAddressExtractor} - Interface for IP extraction strategies</li>
 *   <li>{@link cloud.alchemy.ip.customization.UserIdResolver} - Interface for user ID resolution</li>
 * </ul>
 *
 * <h2>Customization</h2>
 *
 * <p>The library is fully customizable. Provide your own implementations of:
 *
 * <ul>
 *   <li>{@link cloud.alchemy.ip.extractor.IpAddressExtractor} - Custom IP extraction logic</li>
 *   <li>{@link cloud.alchemy.ip.customization.UserIdResolver} - Custom user ID resolution</li>
 *   <li>{@link cloud.alchemy.ip.service.IpAddressStorageService} - Custom storage logic</li>
 *   <li>{@link cloud.alchemy.ip.customization.IpRecordCustomizer} - Record customization before storage</li>
 * </ul>
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see cloud.alchemy.ip.annotation.StoreIPAddress
 * @see cloud.alchemy.ip.config.IpManagementAutoConfiguration
 */
package cloud.alchemy.ip;
