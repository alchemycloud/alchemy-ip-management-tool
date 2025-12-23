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
package cloud.alchemy.ip.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark controller methods for IP address storage.
 *
 * <p>When applied to a method in a class annotated with {@code @RestController}
 * or {@code @Controller}, the IP address of the incoming request will be
 * automatically extracted and stored in the database.
 *
 * <p>Example usage:
 * <pre>{@code
 * @RestController
 * public class UserController {
 *
 *     @StoreIPAddress
 *     @GetMapping("/api/users/{id}")
 *     public User getUser(@PathVariable Long id) {
 *         return userService.findById(id);
 *     }
 *
 *     @StoreIPAddress(storeUserAgent = true, storeRequestPath = true)
 *     @PostMapping("/api/login")
 *     public LoginResponse login(@RequestBody LoginRequest request) {
 *         return authService.login(request);
 *     }
 * }
 * }</pre>
 *
 * <p>The annotation supports various configuration options to control what
 * metadata is stored alongside the IP address.
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see cloud.alchemy.ip.aspect.IpAddressStorageAspect
 * @see cloud.alchemy.ip.entity.IpAddressRecord
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StoreIPAddress {

    /**
     * Specifies whether to store the User-Agent header.
     *
     * @return {@code true} to store User-Agent, {@code false} otherwise
     */
    boolean storeUserAgent() default false;

    /**
     * Specifies whether to store the request path/URI.
     *
     * @return {@code true} to store request path, {@code false} otherwise
     */
    boolean storeRequestPath() default true;

    /**
     * Specifies whether to store the HTTP method (GET, POST, etc.).
     *
     * @return {@code true} to store HTTP method, {@code false} otherwise
     */
    boolean storeHttpMethod() default true;

    /**
     * Specifies whether to store the request timestamp.
     *
     * @return {@code true} to store timestamp, {@code false} otherwise
     */
    boolean storeTimestamp() default true;

    /**
     * Specifies whether to resolve and store geographic location data
     * based on the IP address. Requires a configured geo-location provider.
     *
     * @return {@code true} to store geo-location data, {@code false} otherwise
     */
    boolean storeGeoLocation() default false;

    /**
     * Custom tag or category for this IP address record.
     * Useful for categorizing different types of requests.
     *
     * @return the custom tag, or empty string if not specified
     */
    String tag() default "";

    /**
     * Specifies whether to store the IP address asynchronously.
     * When enabled, the storage operation will not block the request processing.
     *
     * @return {@code true} to store asynchronously, {@code false} for synchronous storage
     */
    boolean async() default true;

    /**
     * Specifies the name of the SpEL expression to extract user ID.
     * If empty, the default user ID extraction strategy will be used.
     *
     * <p>Example: {@code "#{principal.id}"} or {@code "#{@userService.getCurrentUserId()}"}
     *
     * @return the SpEL expression for user ID extraction
     */
    String userIdExpression() default "";
}
