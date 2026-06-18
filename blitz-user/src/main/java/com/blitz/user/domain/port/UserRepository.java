package com.blitz.user.domain.port;

import com.blitz.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    User save(User user);
    boolean existsByUsername(String username);
}
