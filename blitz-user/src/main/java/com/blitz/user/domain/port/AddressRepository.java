package com.blitz.user.domain.port;

import com.blitz.user.domain.model.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    Optional<Address> findById(Long id);
    List<Address> findByUserId(Long userId);
    Address save(Address address);
    void deleteById(Long id);
    void setDefault(Long userId, Long addressId);
}
