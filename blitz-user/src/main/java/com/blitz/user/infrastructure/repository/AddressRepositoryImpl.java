package com.blitz.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blitz.user.domain.model.Address;
import com.blitz.user.domain.port.AddressRepository;
import com.blitz.user.infrastructure.mapper.AddressMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AddressRepositoryImpl implements AddressRepository {

    private final AddressMapper addressMapper;

    public AddressRepositoryImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public Optional<Address> findById(Long id) {
        return Optional.ofNullable(addressMapper.selectById(id));
    }

    @Override
    public List<Address> findByUserId(Long userId) {
        return addressMapper.selectList(
            new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)
        );
    }

    @Override
    public Address save(Address address) {
        if (address.getId() == null) {
            addressMapper.insert(address);
        } else {
            addressMapper.updateById(address);
        }
        return address;
    }

    @Override
    public void deleteById(Long id) {
        addressMapper.deleteById(id);
    }

    @Override
    public void setDefault(Long userId, Long addressId) {
        // Clear existing defaults for this user
        addressMapper.update(null,
            new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
        // Set new default address
        addressMapper.update(null,
            new LambdaUpdateWrapper<Address>()
                .eq(Address::getId, addressId)
                .set(Address::getIsDefault, 1));
    }
}
