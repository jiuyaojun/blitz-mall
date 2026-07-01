package com.blitz.user.api.controller;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.common.result.Result;
import com.blitz.security.service.LoginAttemptService;
import com.blitz.user.api.dto.LoginRequest;
import com.blitz.user.api.dto.RegisterRequest;
import com.blitz.user.api.dto.UserVO;
import com.blitz.user.application.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(userApplicationService.register(req));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        String username = req.username();

        // Check account lockout before attempting login
        if (loginAttemptService.isAccountLocked(username)) {
            long remaining = loginAttemptService.getRemainingLockoutSeconds(username);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED.getCode(),
                "账号已被锁定，请 " + remaining + " 秒后再试");
        }

        try {
            String token = userApplicationService.login(req);
            // Successful login — clear failed attempts
            loginAttemptService.clearAttempts(username);
            UserVO userVO = userApplicationService.findByUsername(req.username());
            return Result.success(Map.of("token", token, "user", userVO));
        } catch (Exception e) {
            // Failed login — record the attempt
            loginAttemptService.recordFailedAttempt(username);
            throw e;
        }
    }

    @GetMapping("/current")
    public Result<UserVO> current() {
        Long userId = getCurrentUserId();
        return Result.success(userApplicationService.getCurrentUser(userId));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Number) return ((Number) principal).longValue();
        throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未登录或登录已过期");
    }
}
