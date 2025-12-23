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
package cloud.alchemy.ip.api.mapper;

import cloud.alchemy.ip.api.dto.IpRecordDetailDto;
import cloud.alchemy.ip.api.dto.IpRecordDto;
import cloud.alchemy.ip.entity.IpAddressRecord;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between IpAddressRecord entity and DTOs.
 */
@Component
public class IpRecordMapper {

    /**
     * Converts an IpAddressRecord entity to a lightweight DTO.
     *
     * @param record the entity to convert
     * @return the DTO
     */
    public IpRecordDto toDto(IpAddressRecord record) {
        if (record == null) {
            return null;
        }
        return new IpRecordDto(
                record.getId(),
                record.getIpAddress(),
                record.getUserId(),
                record.getHttpMethod(),
                record.getRequestPath(),
                record.getTag(),
                record.getCountryCode(),
                record.getCreatedAt()
        );
    }

    /**
     * Converts an IpAddressRecord entity to a full detail DTO.
     *
     * @param record the entity to convert
     * @return the detail DTO
     */
    public IpRecordDetailDto toDetailDto(IpAddressRecord record) {
        if (record == null) {
            return null;
        }
        return new IpRecordDetailDto(
                record.getId(),
                record.getIpAddress(),
                record.getUserId(),
                record.getUserAgent(),
                record.getRequestPath(),
                record.getHttpMethod(),
                record.getTag(),
                record.getCountryCode(),
                record.getCity(),
                record.getRegion(),
                record.getLatitude(),
                record.getLongitude(),
                record.getSourceHeader(),
                record.getCreatedAt(),
                record.getMetadata()
        );
    }
}
