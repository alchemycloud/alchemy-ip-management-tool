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

import cloud.alchemy.ip.config.IpManagementAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables IP address management functionality in a Spring Boot application.
 *
 * <p>Add this annotation to a {@code @Configuration} class to enable
 * automatic IP address storage for methods annotated with {@link StoreIPAddress}.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SpringBootApplication
 * @EnableIpManagement
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * <p>Note: This annotation is optional when using Spring Boot auto-configuration,
 * as the library will be automatically configured. Use this annotation when you
 * need explicit control over the configuration or when auto-configuration is disabled.
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see StoreIPAddress
 * @see IpManagementAutoConfiguration
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(IpManagementAutoConfiguration.class)
public @interface EnableIpManagement {
}
