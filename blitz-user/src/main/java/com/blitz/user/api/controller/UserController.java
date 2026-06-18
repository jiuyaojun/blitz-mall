package com.blitz.user.api.controller;

import com.blitz.common.result.Result;
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

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(userApplicationService.register(req));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        String token = userApplicationService.login(req);
        UserVO userVO = userApplicationService.findByUsername(req.username());
        return Result.success(Map.of("token", token, "user", userVO));
    }

    @GetMapping("/current")
    public Result<UserVO> current() {
        Long userId = getCurrentUserId();
        return Result.success(userApplicationService.getCurrentUser(userId));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((Number) principal).longValue();
    }
}
