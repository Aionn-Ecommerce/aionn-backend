package com.aionn.notification.infrastructure.persistence.repository;

import com.aionn.notification.infrastructure.persistence.entity.DeviceTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenEntity, String> {

    Optional<DeviceTokenEntity> findByUserIdAndDeviceToken(String userId, String deviceToken);

    List<DeviceTokenEntity> findByUserIdAndActiveTrue(String userId);
}

