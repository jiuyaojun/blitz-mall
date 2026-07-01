package com.blitz.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds security-related HTTP response headers to every response.
 * <p>
 * Protects against clickjacking, MIME sniffing, XSS reflection,
 * and enforces HTTPS via HSTS.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Prevent MIME-type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Referrer policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy — restrict resource sources
        // NOTE: 'unsafe-inline' is required by static HTML pages that use inline <script>.
        // For a production site, migrate inline scripts to external .js files with integrity hashes.
        response.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "script-src 'self' 'unsafe-inline' https://unpkg.com; " +
            "style-src 'self' 'unsafe-inline'; " +
            "img-src 'self' data: https:; " +
            "font-src 'self' data:; " +
            "connect-src 'self' https: wss:; " +
            "frame-ancestors 'none';");

        // Permissions policy — disable unnecessary browser features
        response.setHeader("Permissions-Policy",
            "camera=(), microphone=(), geolocation=(), interest-cohort=()");

        // HTTP Strict Transport Security (1 year, include subdomains)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Remove server identity header
        response.setHeader("Server", "");

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Apply to all requests — security headers must be on every response
        return false;
    }
}
