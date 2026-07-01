package com.blitz.security.aspect;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireRoleAspect {

    private final HttpServletRequest request;

    /** Intercept methods annotated with @RequireRole */
    @Before("@annotation(requireRole)")
    public void checkMethodRole(RequireRole requireRole) {
        check(requireRole);
    }

    /** Intercept classes annotated with @RequireRole (fallback for methods without own annotation) */
    @Before("@within(requireRole) && !@annotation(com.blitz.security.annotation.RequireRole)")
    public void checkClassRole(RequireRole requireRole) {
        check(requireRole);
    }

    private void check(RequireRole requireRole) {
        if (requireRole == null) return;
        String role = (String) request.getAttribute("role");
        if (role == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (requireRole.value().length > 0) {
            for (String required : requireRole.value()) {
                if (required.equals(role)) return;                  // exact match
                if ("USER".equals(required) && "MERCHANT".equals(role)) return; // MERCHANT inherits USER
                if ("USER".equals(required) && "ADMIN".equals(role)) return;    // ADMIN inherits USER
                if ("MERCHANT".equals(required) && "ADMIN".equals(role)) return; // ADMIN inherits MERCHANT
            }
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
