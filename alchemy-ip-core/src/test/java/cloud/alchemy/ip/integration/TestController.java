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
package cloud.alchemy.ip.integration;

import cloud.alchemy.ip.annotation.StoreIPAddress;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for integration testing IP address storage.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @StoreIPAddress(async = false)
    @GetMapping("/basic")
    public String basicEndpoint() {
        return "OK";
    }

    @StoreIPAddress(async = false, storeUserAgent = true, storeRequestPath = true, storeHttpMethod = true)
    @GetMapping("/full-metadata")
    public String fullMetadataEndpoint() {
        return "OK with metadata";
    }

    @StoreIPAddress(async = false, tag = "api-access")
    @GetMapping("/tagged")
    public String taggedEndpoint() {
        return "OK with tag";
    }

    @StoreIPAddress(async = true)
    @GetMapping("/async")
    public String asyncEndpoint() {
        return "OK async";
    }

    @StoreIPAddress(async = false, storeUserAgent = true, storeRequestPath = true, storeHttpMethod = true)
    @PostMapping("/post")
    public String postEndpoint() {
        return "POST OK";
    }

    @GetMapping("/no-store")
    public String noStoreEndpoint() {
        return "Not stored";
    }
}
