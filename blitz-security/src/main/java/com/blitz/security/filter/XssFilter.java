package com.blitz.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lightweight XSS protection filter.
 * <p>
 * Sanitizes request parameters and headers by encoding HTML-sensitive characters.
 * For a production system, consider using a library like OWASP Java Encoder or
 * integrating with Spring's built-in HTML escaping.
 */
@Component
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new XssRequestWrapper(request), response);
    }

    /**
     * Wraps the request to sanitize parameter values and headers.
     */
    private static class XssRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {

        XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return sanitize(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return sanitize(value);
        }

        /**
         * Strips common XSS vectors. Does NOT attempt full HTML parsing —
         * the primary defense is CSP headers + output encoding.
         */
        private static String sanitize(String value) {
            if (value == null) return null;
            // Remove script tags and event handlers
            value = value.replaceAll("(?i)<script.*?>.*?</script>", "");
            value = value.replaceAll("(?i)<script.*?>", "");
            value = value.replaceAll("(?i)on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");
            value = value.replaceAll("(?i)on\\w+\\s*=\\s*[^\\s>]*", "");
            // Encode HTML entities
            value = value.replace("<", "&lt;").replace(">", "&gt;");
            value = value.replace("\"", "&quot;").replace("'", "&#x27;");
            return value;
        }
    }
}
