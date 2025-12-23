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

import cloud.alchemy.ip.api.config.IpManagementApiAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Auto-configuration for the IP Management Dashboard UI module.
 */
@AutoConfiguration(after = IpManagementApiAutoConfiguration.class)
@ConditionalOnClass(IpManagementApiAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "alchemy.ip.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IpManagementUiProperties.class)
public class IpManagementUiAutoConfiguration implements WebMvcConfigurer {

    private final IpManagementUiProperties properties;

    public IpManagementUiAutoConfiguration(IpManagementUiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String basePath = properties.getBasePath();
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }

        registry.addResourceHandler(basePath + "**")
                .addResourceLocations("classpath:/META-INF/resources/ip-management/")
                .setCachePeriod(properties.getCacheSeconds())
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource resource = super.getResource(resourcePath, location);
                        // SPA fallback: return index.html for Angular routes
                        if (resource == null || !resource.exists()) {
                            resource = new ClassPathResource("/META-INF/resources/ip-management/index.html");
                        }
                        return resource.exists() ? resource : null;
                    }
                });
    }

    @Bean
    public FilterRegistrationBean<SpaRedirectFilter> spaRedirectFilter() {
        FilterRegistrationBean<SpaRedirectFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SpaRedirectFilter(properties.getBasePath()));
        registration.addUrlPatterns(properties.getBasePath() + "/*");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        registration.setName("spaRedirectFilter");
        return registration;
    }
}
