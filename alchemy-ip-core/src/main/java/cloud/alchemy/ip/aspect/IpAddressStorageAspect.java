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
package cloud.alchemy.ip.aspect;

import cloud.alchemy.ip.annotation.StoreIPAddress;
import cloud.alchemy.ip.customization.UserIdResolver;
import cloud.alchemy.ip.entity.IpAddressRecord;
import cloud.alchemy.ip.extractor.IpAddressExtractor;
import cloud.alchemy.ip.service.IpAddressStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Aspect that intercepts methods annotated with {@link StoreIPAddress}
 * in classes annotated with {@code @RestController} or {@code @Controller}.
 *
 * <p>This aspect extracts the client IP address from the current HTTP request
 * and stores it in the database along with optional metadata.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Alchemy Team
 * @since 1.0.0
 */
@Aspect
public final class IpAddressStorageAspect {

    private static final Logger log = LoggerFactory.getLogger(IpAddressStorageAspect.class);
    private static final int MAX_USER_AGENT_LENGTH = 512;
    private static final int MAX_REQUEST_PATH_LENGTH = 2048;

    private final IpAddressStorageService storageService;
    private final IpAddressExtractor ipAddressExtractor;
    private final UserIdResolver userIdResolver;
    private final BeanFactory beanFactory;
    private final ExpressionParser expressionParser;

    /**
     * Creates a new aspect with the specified dependencies.
     *
     * @param storageService     the IP address storage service
     * @param ipAddressExtractor the IP address extractor
     * @param userIdResolver     the user ID resolver
     * @param beanFactory        the Spring bean factory for SpEL evaluation
     */
    public IpAddressStorageAspect(IpAddressStorageService storageService,
                                   IpAddressExtractor ipAddressExtractor,
                                   UserIdResolver userIdResolver,
                                   BeanFactory beanFactory) {
        this.storageService = storageService;
        this.ipAddressExtractor = ipAddressExtractor;
        this.userIdResolver = userIdResolver;
        this.beanFactory = beanFactory;
        this.expressionParser = new SpelExpressionParser();
    }

    /**
     * Intercepts methods annotated with @StoreIPAddress in @RestController or @Controller classes.
     *
     * @param joinPoint       the join point
     * @param storeIPAddress  the annotation instance
     * @return the result of the method execution
     * @throws Throwable if the method execution fails
     */
    @Around("@annotation(storeIPAddress) && " +
            "(@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller))")
    public Object aroundControllerMethod(ProceedingJoinPoint joinPoint,
                                         StoreIPAddress storeIPAddress) throws Throwable {
        final HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.warn("No HTTP request available in current context, skipping IP storage");
            return joinPoint.proceed();
        }

        final Object result = joinPoint.proceed();

        try {
            storeIpAddress(request, storeIPAddress, joinPoint, result);
        } catch (Exception e) {
            log.error("Failed to store IP address, but not affecting the request processing", e);
        }

        return result;
    }

    private void storeIpAddress(HttpServletRequest request,
                                StoreIPAddress annotation,
                                ProceedingJoinPoint joinPoint,
                                Object methodResult) {
        final Optional<String> ipAddressOpt = ipAddressExtractor.extractIpAddress(request);
        if (ipAddressOpt.isEmpty()) {
            log.warn("Could not extract IP address from request");
            return;
        }

        final String ipAddress = ipAddressOpt.get();
        final String userId = resolveUserId(request, annotation, joinPoint, methodResult);

        final IpAddressRecord.Builder recordBuilder = IpAddressRecord.builder()
                .ipAddress(ipAddress)
                .userId(userId);

        if (annotation.storeUserAgent()) {
            recordBuilder.userAgent(truncate(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH));
        }

        if (annotation.storeRequestPath()) {
            recordBuilder.requestPath(truncate(request.getRequestURI(), MAX_REQUEST_PATH_LENGTH));
        }

        if (annotation.storeHttpMethod()) {
            recordBuilder.httpMethod(request.getMethod());
        }

        if (!annotation.tag().isEmpty()) {
            recordBuilder.tag(annotation.tag());
        }

        final IpAddressRecord record = recordBuilder.build();

        if (annotation.async()) {
            storageService.storeAsync(record)
                    .thenAccept(savedOpt -> {
                        if (savedOpt.isPresent()) {
                            log.debug("Async IP address stored: {}", ipAddress);
                        } else {
                            log.debug("Async IP address skipped (duplicate): {}", ipAddress);
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("Async IP storage failed for IP: {}", ipAddress, ex);
                        return null;
                    });
        } else {
            final Optional<IpAddressRecord> savedOpt = storageService.store(record);
            if (savedOpt.isPresent()) {
                log.debug("IP address stored: {}", ipAddress);
            } else {
                log.debug("IP address skipped (duplicate): {}", ipAddress);
            }
        }
    }

    private String resolveUserId(HttpServletRequest request,
                                 StoreIPAddress annotation,
                                 ProceedingJoinPoint joinPoint,
                                 Object methodResult) {
        final String spelExpression = annotation.userIdExpression();
        if (spelExpression != null && !spelExpression.isBlank()) {
            try {
                final StandardEvaluationContext context = new StandardEvaluationContext();
                context.setBeanResolver(new BeanFactoryResolver(beanFactory));
                context.setVariable("request", request);
                context.setVariable("result", methodResult);
                context.setVariable("args", joinPoint.getArgs());

                final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                final String[] paramNames = signature.getParameterNames();
                final Object[] args = joinPoint.getArgs();
                if (paramNames != null) {
                    for (int i = 0; i < paramNames.length; i++) {
                        context.setVariable(paramNames[i], args[i]);
                    }
                }

                final Object result = expressionParser.parseExpression(spelExpression).getValue(context);
                if (result != null) {
                    return result.toString();
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate SpEL expression '{}': {}", spelExpression, e.getMessage());
            }
        }

        return userIdResolver.resolveUserId(request).orElse(null);
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            final ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            log.debug("No request context available: {}", e.getMessage());
            return null;
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
