package com.blitz.security.aspect;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.security.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireRoleAspect {

    private final HttpServletRequest request;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        String role = (String) request.getAttribute("role");
        if (role == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (requireRole.value().length > 0 && !Arrays.asList(requireRole.value()).contains(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
