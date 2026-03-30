package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.address.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.UpdateAddressCommand;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.Address;
import com.ecommerce.identity.infrastructure.persistence.entity.UserAddressEntity;
import com.ecommerce.identity.infrastructure.persistence.entity.UserEntity;
import com.ecommerce.identity.infrastructure.persistence.mapper.AddressDomainMapper;
import com.ecommerce.identity.infrastructure.persistence.repository.address.AddressRepository;
import com.ecommerce.identity.infrastructure.persistence.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

	@Value("${identity.address.max-address-numbers:5}")
	private long maxAddresses;

	private final AddressRepository addressRepository;
	private final UserRepository userRepository;
	private final AddressDomainMapper addressDomainMapper;
	private final GeographyService geographyService;

	@Transactional(readOnly = true)
	public List<Address> listAddressesByUserId(String userId) {
		return addressRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(addressDomainMapper::toDomain)
				.toList();
	}

	@Transactional
	public Address createAddress(CreateAddressCommand command) {
		var user = getUserOrThrow(command.userId());
		long count = addressRepository.countByUser_UserId(command.userId());
		if (count >= maxAddresses) {
			throw new IdentityException(IdentityErrorCode.ADDRESS_NUMBER_EXCEEDED);
		}

		// Validate geography codes and build full address
		String fullAddress = buildFullAddress(command.provinceCode(), command.districtCode(),
				command.wardCode(), command.detailAddress());

		boolean shouldSetAsDefault = (count == 0) || command.isDefault();
		if (shouldSetAsDefault) {
			clearCurrentDefaultAddress(command.userId());
		}

		UserAddressEntity entity = addressDomainMapper.toEntity(command, user, shouldSetAsDefault);
		entity.setFullAddress(fullAddress);
		var saved = addressRepository.save(entity);
		return addressDomainMapper.toDomain(saved);
	}

	@Transactional
	public Address updateAddress(UpdateAddressCommand command) {
		UserAddressEntity address = getAddressOrThrow(command.userId(), command.addressId());

		// Validate geography codes and build full address
		String fullAddress = buildFullAddress(command.provinceCode(), command.districtCode(),
				command.wardCode(), command.detailAddress());

		addressDomainMapper.updateEntityFromCommand(command, address);
		address.setFullAddress(fullAddress);
		return addressDomainMapper.toDomain(addressRepository.save(address));
	}

	@Transactional
	public void deleteAddress(String userId, String addressId) {
		UserAddressEntity address = getAddressOrThrow(userId, addressId);
		Address domainAddress = addressDomainMapper.toDomain(address);
		if (!domainAddress.canBeDeleted()) {
			throw new IdentityException(IdentityErrorCode.VERIFICATION_TOKEN_INVALID,
					"Default address cannot be deleted");
		}
		addressRepository.delete(address);
	}

	@Transactional
	public Address setDefaultAddress(String userId, String addressId) {
		UserAddressEntity targetEntity = getAddressOrThrow(userId, addressId);
		if (Boolean.TRUE.equals(targetEntity.getIsDefault())) {
			return addressDomainMapper.toDomain(targetEntity);
		}
		clearCurrentDefaultAddress(userId);
		targetEntity.setIsDefault(Boolean.TRUE);
		return addressDomainMapper.toDomain(addressRepository.save(targetEntity));
	}

	// helper function
	private UserAddressEntity getAddressOrThrow(String userId, String addressId) {
		return addressRepository.findByAddressIdAndUser_UserId(addressId, userId)
				.orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND, "Address not found"));
	}

	private void clearCurrentDefaultAddress(String userId) {
		addressRepository.findByUser_UserIdAndIsDefaultTrue(userId).ifPresent(defaultAddress -> {
			defaultAddress.setIsDefault(Boolean.FALSE);
			addressRepository.save(defaultAddress);
		});
	}

	private UserEntity getUserOrThrow(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IdentityException(IdentityErrorCode.USER_NOT_FOUND));
	}

	private String buildFullAddress(String provinceCode, String districtCode, String wardCode, String detailAddress) {
		// Validate and get names from geography service
		var province = geographyService.getProvince(provinceCode)
				.orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_INPUT,
						"Invalid province code: " + provinceCode));

		var district = geographyService.getDistrict(districtCode)
				.orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_INPUT,
						"Invalid district code: " + districtCode));

		var ward = geographyService.getWard(wardCode)
				.orElseThrow(
						() -> new IdentityException(IdentityErrorCode.INVALID_INPUT, "Invalid ward code: " + wardCode));

		// Build full address string with names
		return String.join(", ", detailAddress, ward.name(), district.name(), province.name());
	}

}
