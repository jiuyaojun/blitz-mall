package com.blitz.user.api.controller;

import com.blitz.common.result.Result;
import com.blitz.user.api.dto.AddressDTO;
import com.blitz.user.api.dto.AddressVO;
import com.blitz.user.application.service.AddressApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressApplicationService addressApplicationService;

    @GetMapping
    public Result<List<AddressVO>> list() {
        Long userId = getCurrentUserId();
        return Result.success(addressApplicationService.listByUserId(userId));
    }

    @PostMapping
    public Result<AddressVO> add(@Valid @RequestBody AddressDTO dto) {
        Long userId = getCurrentUserId();
        return Result.success(addressApplicationService.add(userId, dto));
    }

    @PutMapping("/{id}")
    public Result<AddressVO> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        Long userId = getCurrentUserId();
        return Result.success(addressApplicationService.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressApplicationService.delete(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ((Number) principal).longValue();
    }
}
