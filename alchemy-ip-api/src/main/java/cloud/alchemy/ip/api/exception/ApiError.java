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
package cloud.alchemy.ip.api.exception;

import java.time.Instant;

/**
 * Standard API error response.
 *
 * @param timestamp the error timestamp
 * @param status    the HTTP status code
 * @param error     the error type
 * @param message   the error message
 * @param path      the request path
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {

    /**
     * Creates an ApiError with the current timestamp.
     *
     * @param status  the HTTP status code
     * @param error   the error type
     * @param message the error message
     * @param path    the request path
     * @return a new ApiError
     */
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path);
    }
}
