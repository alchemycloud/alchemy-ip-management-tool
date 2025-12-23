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

import cloud.alchemy.ip.aspect.IpAddressStorageAspect;
import cloud.alchemy.ip.customization.DefaultUserIdResolver;
import cloud.alchemy.ip.customization.IpRecordCustomizer;
import cloud.alchemy.ip.customization.UserIdResolver;
import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.extractor.DefaultIpAddressExtractor;
import cloud.alchemy.ip.extractor.IpAddressExtractor;
import cloud.alchemy.ip.repository.IpAddressRepository;
import cloud.alchemy.ip.service.DefaultIpAddressStorageService;
import cloud.alchemy.ip.service.IpAddressStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Auto-configuration for the Alchemy IP Management Tool.
 *
 * <p>This configuration automatically sets up all required beans when
 * the library is included in a Spring Boot application with JPA and
 * a web environment.
 *
 * <p>The auto-configuration can be disabled by setting
 * {@code alchemy.ip.enabled=false} in the application properties.
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass({EntityManager.class})
@ConditionalOnProperty(prefix = "alchemy.ip", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IpManagementProperties.class)
@EnableJpaRepositories(basePackageClasses = IpAddressRepository.class)
@EntityScan(basePackageClasses = IpAddressRecord.class)
@EnableAspectJAutoProxy
@EnableAsync
public class IpManagementAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IpManagementAutoConfiguration.class);

    private final IpManagementProperties properties;

    /**
     * Creates the auto-configuration with the specified properties.
     *
     * @param properties the IP management properties
     */
    public IpManagementAutoConfiguration(IpManagementProperties properties) {
        this.properties = properties;
        log.info("Initializing Alchemy IP Management Tool");
    }

    /**
     * Creates the default IP address extractor.
     * Can be overridden by providing a custom {@link IpAddressExtractor} bean.
     *
     * @return the IP address extractor
     */
    @Bean
    @ConditionalOnMissingBean(IpAddressExtractor.class)
    public IpAddressExtractor ipAddressExtractor() {
        log.debug("Creating default IP address extractor");
        return new DefaultIpAddressExtractor(
                properties.isTrustAllProxies(),
                properties.getTrustedProxies()
        );
    }

    /**
     * Creates the default user ID resolver.
     * Can be overridden by providing a custom {@link UserIdResolver} bean.
     *
     * @return the user ID resolver
     */
    @Bean
    @ConditionalOnMissingBean(UserIdResolver.class)
    public UserIdResolver userIdResolver() {
        log.debug("Creating default user ID resolver");
        return new DefaultUserIdResolver();
    }

    /**
     * Creates the default IP address storage service.
     * Can be overridden by providing a custom {@link IpAddressStorageService} bean.
     *
     * @param repository         the IP address repository
     * @param ipAddressExtractor the IP address extractor
     * @param customizers        optional list of record customizers
     * @return the IP address storage service
     */
    @Bean
    @ConditionalOnMissingBean(IpAddressStorageService.class)
    public IpAddressStorageService ipAddressStorageService(
            IpAddressRepository repository,
            IpAddressExtractor ipAddressExtractor,
            ObjectProvider<List<IpRecordCustomizer>> customizers) {
        log.debug("Creating default IP address storage service");
        return new DefaultIpAddressStorageService(repository, ipAddressExtractor);
    }

    /**
     * Creates the IP address storage aspect that intercepts annotated methods.
     *
     * @param storageService     the IP address storage service
     * @param ipAddressExtractor the IP address extractor
     * @param userIdResolver     the user ID resolver
     * @param beanFactory        the bean factory for SpEL evaluation
     * @return the storage aspect
     */
    @Bean
    @ConditionalOnMissingBean(IpAddressStorageAspect.class)
    public IpAddressStorageAspect ipAddressStorageAspect(
            IpAddressStorageService storageService,
            IpAddressExtractor ipAddressExtractor,
            UserIdResolver userIdResolver,
            BeanFactory beanFactory) {
        log.debug("Creating IP address storage aspect");
        return new IpAddressStorageAspect(
                storageService,
                ipAddressExtractor,
                userIdResolver,
                beanFactory
        );
    }

    /**
     * Creates the async task executor for non-blocking IP storage operations.
     *
     * @return the task executor
     */
    @Bean(name = "ipManagementTaskExecutor")
    @ConditionalOnProperty(prefix = "alchemy.ip.async", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Executor ipManagementTaskExecutor() {
        log.debug("Creating async task executor for IP management");
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        final IpManagementProperties.Async asyncConfig = properties.getAsync();
        executor.setCorePoolSize(asyncConfig.getCorePoolSize());
        executor.setMaxPoolSize(asyncConfig.getMaxPoolSize());
        executor.setQueueCapacity(asyncConfig.getQueueCapacity());
        executor.setThreadNamePrefix(asyncConfig.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Creates the schema initializer that handles table creation and
     * foreign key setup.
     *
     * @param dataSource the data source
     * @return the initialization bean
     */
    @Bean
    @ConditionalOnMissingBean(IpManagementSchemaInitializer.class)
    @ConditionalOnProperty(prefix = "alchemy.ip.table", name = "auto-create", havingValue = "true", matchIfMissing = true)
    public IpManagementSchemaInitializer ipManagementSchemaInitializer(DataSource dataSource) {
        log.debug("Creating IP management schema initializer");
        return new IpManagementSchemaInitializer(dataSource, properties);
    }
}
