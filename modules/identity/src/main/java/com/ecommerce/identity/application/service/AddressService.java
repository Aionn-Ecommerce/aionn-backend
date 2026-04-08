package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.address.command.CreateAddressCommand;
import com.ecommerce.identity.application.dto.address.command.UpdateAddressCommand;
import com.ecommerce.identity.application.dto.geography.result.ResolvedLocation;
import com.ecommerce.identity.application.port.out.address.AddressPersistencePort;
import com.ecommerce.identity.application.port.out.address.AddressPolicy;
import com.ecommerce.identity.application.port.out.user.UserPersistencePort;
import com.ecommerce.identity.domain.exception.IdentityErrorCode;
import com.ecommerce.identity.domain.exception.IdentityException;
import com.ecommerce.identity.domain.model.Address;
import com.ecommerce.sharedkernel.domain.vo.PhoneNumber;
import com.ecommerce.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

	private final AddressPersistencePort addressPersistencePort;
	private final UserPersistencePort userPersistencePort;
	private final AddressPolicy addressPolicy;
	private final GeographyService geographyService;

	public List<Address> listAddressesByUserId(String userId) {
		log.debug("Listing addresses for user: {}", userId);
		return addressPersistencePort.findByUserId(userId);
	}

	public Address createAddress(CreateAddressCommand command) {
		log.info("Creating address for user: {}", command.userId());

		// Validate phone number format
		PhoneNumber.of(command.phone());

		if (!userPersistencePort.existsById(command.userId())) {
			throw new IdentityException(IdentityErrorCode.USER_NOT_FOUND);
		}

		long count = addressPersistencePort.countByUserId(command.userId());
		if (count >= addressPolicy.getMaxAddressNumbers()) {
			throw new IdentityException(IdentityErrorCode.ADDRESS_NUMBER_EXCEEDED);
		}

		ResolvedLocation location = geographyService.resolveLocation(
				command.provinceCode(), command.districtCode(), command.wardCode());

		String fullAddress = location.buildFullAddress(command.detailAddress());

		boolean shouldSetAsDefault = (count == 0) || command.isDefault();
		if (shouldSetAsDefault) {
			addressPersistencePort.clearDefaultByUserId(command.userId());
		}

		Address address = new Address(
				IdGenerator.ulid(),
				command.userId(),
				command.contactName(),
				command.phone(),
				command.provinceCode(),
				location.province().name(),
				command.districtCode(),
				location.district().name(),
				command.wardCode(),
				location.ward().name(),
				command.detailAddress(),
				fullAddress,
				command.type(),
				shouldSetAsDefault,
				LocalDateTime.now(),
				LocalDateTime.now());

		return addressPersistencePort.save(address);
	}

	public Address updateAddress(UpdateAddressCommand command) {
		log.info("Updating address: {} for user: {}", command.addressId(), command.userId());

		// Validate phone number format
		PhoneNumber.of(command.phone());

		Address address = getAddressOrThrow(command.userId(), command.addressId());

		String fullAddress = address.fullAddress();
		String provinceName = address.provinceName();
		String districtName = address.districtName();
		String wardName = address.wardName();

		boolean locationChanged = !command.provinceCode().equals(address.provinceCode())
				|| !command.districtCode().equals(address.districtCode())
				|| !command.wardCode().equals(address.wardCode())
				|| !command.detailAddress().equals(address.detailAddress());

		if (locationChanged) {
			ResolvedLocation location = geographyService.resolveLocation(
					command.provinceCode(), command.districtCode(), command.wardCode());

			fullAddress = location.buildFullAddress(command.detailAddress());
			provinceName = location.province().name();
			districtName = location.district().name();
			wardName = location.ward().name();
		}

		Address updatedAddress = new Address(
				address.addressId(),
				address.userId(),
				command.contactName(),
				command.phone(),
				command.provinceCode(),
				provinceName,
				command.districtCode(),
				districtName,
				command.wardCode(),
				wardName,
				command.detailAddress(),
				fullAddress,
				command.type(),
				address.isDefault(),
				address.createdAt(),
				LocalDateTime.now());

		return addressPersistencePort.save(updatedAddress);
	}

	public void deleteAddress(String userId, String addressId) {
		log.info("Deleting address: {} for user: {}", addressId, userId);
		Address address = getAddressOrThrow(userId, addressId);
		if (!address.canBeDeleted()) {
			throw new IdentityException(IdentityErrorCode.DEFAULT_ADDRESS_CANNOT_BE_DELETED);
		}
		addressPersistencePort.delete(address);
	}

	public Address setDefaultAddress(String userId, String addressId) {
		log.info("Setting default address: {} for user: {}", addressId, userId);
		Address address = getAddressOrThrow(userId, addressId);
		if (address.isDefault()) {
			return address;
		}
		addressPersistencePort.clearDefaultByUserId(userId);

		Address updatedAddress = new Address(
				address.addressId(),
				address.userId(),
				address.contactName(),
				address.phone(),
				address.provinceCode(),
				address.provinceName(),
				address.districtCode(),
				address.districtName(),
				address.wardCode(),
				address.wardName(),
				address.detailAddress(),
				address.fullAddress(),
				address.type(),
				true,
				address.createdAt(),
				LocalDateTime.now());

		return addressPersistencePort.save(updatedAddress);
	}

	private Address getAddressOrThrow(String userId, String addressId) {
		return addressPersistencePort.findByAddressIdAndUserId(addressId, userId)
				.orElseThrow(() -> new IdentityException(IdentityErrorCode.ADDRESS_NOT_FOUND, "Address not found"));
	}
}
