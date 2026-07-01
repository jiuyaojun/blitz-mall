package com.blitz.security.filter;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.security.service.LoginAttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Rate-limiting filter for sensitive endpoints (login, register).
 * <p>
 * Uses in-memory IP + endpoint counters with a simple sliding-window approach.
 * Combined with {@link LoginAttemptService} for account-level lockout.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;

    /** Sensitive paths subject to rate limiting */
    private static final String[] RATE_LIMITED_PATHS = {
        "/user/login",
        "/user/register"
    };

    /** Max requests per minute per IP for login endpoints */
    private static final int LOGIN_RATE_LIMIT = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isRateLimited(path)) {
            String clientIp = getClientIp(request);

            // Check IP-level rate limit
            if (loginAttemptService.isIpRateLimited(clientIp, path, LOGIN_RATE_LIMIT)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    "{\"code\":" + ErrorCode.RATE_LIMITED.getCode() +
                    ",\"msg\":\"" + ErrorCode.RATE_LIMITED.getMsg() + "\"}");
                return;
            }

            // Check account lockout — wrap request to cache body for JSON reading
            if ("/user/login".equals(path) && "application/json".equals(request.getContentType())) {
                CachedBodyRequestWrapper wrapper = new CachedBodyRequestWrapper(request);
                String username = extractUsername(wrapper);
                if (username != null && loginAttemptService.isAccountLocked(username)) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"code\":" + ErrorCode.ACCOUNT_LOCKED.getCode() +
                        ",\"msg\":\"" + ErrorCode.ACCOUNT_LOCKED.getMsg() + "\"}");
                    return;
                }
                // Pass wrapper to chain so downstream can still read the body
                filterChain.doFilter(wrapper, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Extract username from cached request body or query parameters */
    private String extractUsername(HttpServletRequest request) {
        // Try query/form parameter first
        String username = request.getParameter("username");
        if (username != null) return username;

        // Try reading from cached body
        if (request instanceof CachedBodyRequestWrapper) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = objectMapper.readValue(
                    ((CachedBodyRequestWrapper) request).getBody(), Map.class);
                Object user = body.get("username");
                return user != null ? user.toString() : null;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    /** Request wrapper that caches the body so it can be read multiple times */
    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        public CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            this.body = sb.toString().getBytes(StandardCharsets.UTF_8);
        }

        public String getBody() { return new String(body, StandardCharsets.UTF_8); }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public int read() { return bais.read(); }
                @Override public boolean isFinished() { return bais.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(jakarta.servlet.ReadListener listener) {}
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isRateLimited(request.getRequestURI());
    }

    private boolean isRateLimited(String path) {
        for (String p : RATE_LIMITED_PATHS) {
            if (path.equals(p)) return true;
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        // Trust X-Real-IP set by nginx first (nginx strips spoofed headers)
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // For X-Forwarded-For, take the RIGHTMOST IP (added by our own proxy, not client-spoofed)
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
                String[] ips = xff.split(",");
                ip = ips[ips.length - 1].trim();
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
