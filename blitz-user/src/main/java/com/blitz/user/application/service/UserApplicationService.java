package com.blitz.user.application.service;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.security.util.JwtUtil;
import com.blitz.user.api.dto.LoginRequest;
import com.blitz.user.api.dto.RegisterRequest;
import com.blitz.user.api.dto.UserVO;
import com.blitz.user.domain.model.User;
import com.blitz.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserVO register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "用户名已存在");
        }
        if (req.phone() != null && !req.phone().isBlank()
                && userRepository.findByPhone(req.phone()).isPresent()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID.getCode(), "手机号已被注册");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setPhone(req.phone());
        user.setEmail(req.email());
        user.setRoleId(2L);
        user.setStatus(1);
        userRepository.save(user);
        return toVO(user);
    }

    @Transactional
    public String login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.isDisabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        user.recordLogin();
        userRepository.save(user);
        return jwtUtil.createToken(user.getId(), user.getUsername(), resolveRole(user.getRoleId()));
    }

    public UserVO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return toVO(user);
    }

    public UserVO findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return toVO(user);
    }

    private UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getPhone(),
                user.getEmail(), user.getAvatar(), user.getRoleId(),
                user.getStatus(), user.getCreateTime());
    }

    private String resolveRole(Long roleId) {
        return roleId != null && roleId == 1L ? "ADMIN" : "USER";
    }
}
