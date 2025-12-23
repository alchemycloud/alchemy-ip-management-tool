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

/**
 * Exception thrown when an IP record is not found.
 */
public class RecordNotFoundException extends RuntimeException {

    private final Long recordId;

    public RecordNotFoundException(Long recordId) {
        super("IP record not found with id: " + recordId);
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }
}
