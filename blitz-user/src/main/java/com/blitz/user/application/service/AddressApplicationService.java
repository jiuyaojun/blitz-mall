package com.blitz.user.application.service;

import com.blitz.common.exception.BusinessException;
import com.blitz.common.exception.ErrorCode;
import com.blitz.user.api.dto.AddressDTO;
import com.blitz.user.api.dto.AddressVO;
import com.blitz.user.domain.model.Address;
import com.blitz.user.domain.port.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressApplicationService {

    private final AddressRepository addressRepository;

    public List<AddressVO> listByUserId(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toVO).toList();
    }

    @Transactional
    public AddressVO add(Long userId, AddressDTO dto) {
        Address address = new Address();
        address.setUserId(userId);
        applyDTO(address, dto);
        addressRepository.save(address);
        if (dto.isDefault() != null && dto.isDefault() == 1) {
            addressRepository.setDefault(userId, address.getId());
            address.setIsDefault(1);
        }
        return toVO(address);
    }

    @Transactional
    public AddressVO update(Long userId, Long addressId, AddressDTO dto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        applyDTO(address, dto);
        addressRepository.save(address);
        if (dto.isDefault() != null && dto.isDefault() == 1) {
            addressRepository.setDefault(userId, address.getId());
            address.setIsDefault(1);
        }
        return toVO(address);
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        addressRepository.deleteById(addressId);
    }

    private void applyDTO(Address address, AddressDTO dto) {
        address.setReceiverName(dto.receiverName());
        address.setReceiverPhone(dto.receiverPhone());
        address.setProvince(dto.province());
        address.setCity(dto.city());
        address.setDistrict(dto.district());
        address.setDetail(dto.detail());
        if (dto.isDefault() != null) {
            address.setIsDefault(dto.isDefault());
        }
    }

    private AddressVO toVO(Address address) {
        return new AddressVO(address.getId(), address.getUserId(),
                address.getReceiverName(), address.getReceiverPhone(),
                address.getProvince(), address.getCity(),
                address.getDistrict(), address.getDetail(),
                address.getIsDefault());
    }
}
