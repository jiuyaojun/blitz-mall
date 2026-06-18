package com.blitz.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blitz.user.domain.model.User;
import com.blitz.user.domain.port.UserRepository;
import com.blitz.user.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(
            userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
            )
        );
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return Optional.ofNullable(
            userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
            )
        );
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.exists(
            new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
    }
}
