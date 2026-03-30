package com.ecommerce.identity.infrastructure.persistence.repository.address;

import com.ecommerce.identity.infrastructure.persistence.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<UserAddressEntity, String> {

    List<UserAddressEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    Optional<UserAddressEntity> findByAddressIdAndUser_UserId(String addressId, String userId);

    Optional<UserAddressEntity> findByUser_UserIdAndIsDefaultTrue(String userId);

    long countByUser_UserId(String userId);
}
