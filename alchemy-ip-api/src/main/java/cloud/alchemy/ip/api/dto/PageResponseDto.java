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
package cloud.alchemy.ip.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Standardized pagination response DTO.
 *
 * @param content       the page content
 * @param pageNumber    current page number (0-indexed)
 * @param pageSize      page size
 * @param totalElements total number of elements
 * @param totalPages    total number of pages
 * @param first         whether this is the first page
 * @param last          whether this is the last page
 * @param <T>           the content element type
 */
public record PageResponseDto<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /**
     * Creates a PageResponseDto from a Spring Data Page.
     *
     * @param page the Spring Data Page
     * @param <T>  the content element type
     * @return a new PageResponseDto
     */
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    /**
     * Creates a PageResponseDto from a Spring Data Page with content mapping.
     *
     * @param page   the Spring Data Page
     * @param mapper function to map page content
     * @param <T>    the source content element type
     * @param <R>    the target content element type
     * @return a new PageResponseDto with mapped content
     */
    public static <T, R> PageResponseDto<R> from(Page<T> page, Function<T, R> mapper) {
        return new PageResponseDto<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
