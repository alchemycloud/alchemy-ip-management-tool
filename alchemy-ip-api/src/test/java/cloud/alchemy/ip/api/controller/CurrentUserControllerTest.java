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
package cloud.alchemy.ip.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link CurrentUserController}.
 */
@WebMvcTest(CurrentUserController.class)
@Import(CurrentUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CurrentUserController")
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/alchemy-ip/current-user")
    class GetCurrentUser {

        @Test
        @DisplayName("should return authenticated user info when logged in")
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN", "USER"})
        void shouldReturnAuthenticatedUserInfoWhenLoggedIn() throws Exception {
            mockMvc.perform(get("/api/alchemy-ip/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated", is(true)))
                    .andExpect(jsonPath("$.username", is("admin@example.com")))
                    .andExpect(jsonPath("$.authorities", hasSize(2)))
                    .andExpect(jsonPath("$.authorities", hasItem("ROLE_ADMIN")))
                    .andExpect(jsonPath("$.authorities", hasItem("ROLE_USER")));
        }

        @Test
        @DisplayName("should return single role user correctly")
        @WithMockUser(username = "user@example.com", roles = "USER")
        void shouldReturnSingleRoleUserCorrectly() throws Exception {
            mockMvc.perform(get("/api/alchemy-ip/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated", is(true)))
                    .andExpect(jsonPath("$.username", is("user@example.com")))
                    .andExpect(jsonPath("$.authorities", hasSize(1)))
                    .andExpect(jsonPath("$.authorities[0]", is("ROLE_USER")));
        }

        @Test
        @DisplayName("should return unauthenticated when anonymous user")
        @WithAnonymousUser
        void shouldReturnUnauthenticatedWhenAnonymousUser() throws Exception {
            mockMvc.perform(get("/api/alchemy-ip/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated", is(false)))
                    .andExpect(jsonPath("$.username").doesNotExist())
                    .andExpect(jsonPath("$.authorities").doesNotExist());
        }

        @Test
        @DisplayName("should handle user with authorities instead of roles")
        @WithMockUser(username = "service@example.com", authorities = {"READ_RECORDS", "WRITE_RECORDS"})
        void shouldHandleUserWithAuthoritiesInsteadOfRoles() throws Exception {
            mockMvc.perform(get("/api/alchemy-ip/current-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authenticated", is(true)))
                    .andExpect(jsonPath("$.username", is("service@example.com")))
                    .andExpect(jsonPath("$.authorities", hasSize(2)))
                    .andExpect(jsonPath("$.authorities", hasItem("READ_RECORDS")))
                    .andExpect(jsonPath("$.authorities", hasItem("WRITE_RECORDS")));
        }
    }
}
