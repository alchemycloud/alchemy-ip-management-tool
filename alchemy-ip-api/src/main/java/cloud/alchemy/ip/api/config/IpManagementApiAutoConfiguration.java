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
package cloud.alchemy.ip.api.config;

import cloud.alchemy.ip.api.controller.CurrentUserController;
import cloud.alchemy.ip.api.controller.IpRecordController;
import cloud.alchemy.ip.api.controller.IpStatsController;
import cloud.alchemy.ip.api.exception.ApiExceptionHandler;
import cloud.alchemy.ip.api.mapper.IpRecordMapper;
import cloud.alchemy.ip.repository.IpAddressRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Auto-configuration for the IP Management REST API module.
 */
@AutoConfiguration
@ConditionalOnClass({DispatcherServlet.class, IpAddressRepository.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "alchemy.ip.api", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IpManagementApiProperties.class)
@Import({CorsConfig.class})
public class IpManagementApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IpRecordMapper ipRecordMapper() {
        return new IpRecordMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public IpRecordController ipRecordController(IpAddressRepository repository, IpRecordMapper mapper) {
        return new IpRecordController(repository, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public IpStatsController ipStatsController(IpAddressRepository repository, IpRecordMapper mapper) {
        return new IpStatsController(repository, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    public CurrentUserController currentUserController() {
        return new CurrentUserController();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiExceptionHandler apiExceptionHandler() {
        return new ApiExceptionHandler();
    }
}
