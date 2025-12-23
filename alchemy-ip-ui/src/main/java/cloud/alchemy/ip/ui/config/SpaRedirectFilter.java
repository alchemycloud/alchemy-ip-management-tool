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
package cloud.alchemy.ip.ui.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filter to handle Angular SPA routing by forwarding non-static requests to index.html.
 */
public class SpaRedirectFilter extends OncePerRequestFilter {

    private static final Set<String> STATIC_EXTENSIONS = Set.of(
            ".js", ".css", ".ico", ".png", ".jpg", ".jpeg", ".gif", ".svg",
            ".woff", ".woff2", ".ttf", ".eot", ".map", ".json", ".html"
    );

    private final String basePath;

    public SpaRedirectFilter(String basePath) {
        this.basePath = basePath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Only handle requests under the dashboard base path
        if (!path.startsWith(basePath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow static resources through
        if (isStaticResource(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow API calls through
        if (path.contains("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Forward Angular routes to index.html
        String indexPath = basePath + "/index.html";
        if (!path.equals(indexPath)) {
            request.getRequestDispatcher(indexPath).forward(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isStaticResource(String path) {
        return STATIC_EXTENSIONS.stream().anyMatch(ext -> path.toLowerCase().endsWith(ext));
    }
}
