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
package cloud.alchemy.ip.customization;

import cloud.alchemy.ip.entity.IpAddressRecord;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Callback interface for customizing IP address records before they are persisted.
 *
 * <p>Implementations can modify the record or add additional data before storage.
 * Multiple customizers can be registered and will be called in order.
 *
 * <p>Example implementation adding geo-location data:
 * <pre>{@code
 * @Component
 * public class GeoLocationCustomizer implements IpRecordCustomizer {
 *
 *     private final GeoLocationService geoService;
 *
 *     @Override
 *     public void customize(IpAddressRecord record, HttpServletRequest request) {
 *         GeoLocation location = geoService.lookup(record.getIpAddress());
 *         if (location != null) {
 *             record.setCountryCode(location.getCountryCode());
 *             record.setCity(location.getCity());
 *             record.setLatitude(location.getLatitude());
 *             record.setLongitude(location.getLongitude());
 *         }
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return 100; // Run after default customizers
 *     }
 * }
 * }</pre>
 *
 * @author Alchemy Team
 * @since 1.0.0
 * @see IpAddressRecord
 */
@FunctionalInterface
public interface IpRecordCustomizer {

    /**
     * Customizes the IP address record before it is persisted.
     *
     * @param record  the IP address record to customize
     * @param request the HTTP servlet request (may be null in some contexts)
     */
    void customize(IpAddressRecord record, HttpServletRequest request);

    /**
     * Returns the order in which this customizer should be applied.
     * Lower values have higher priority. Default is 0.
     *
     * @return the order value
     */
    default int getOrder() {
        return 0;
    }
}
