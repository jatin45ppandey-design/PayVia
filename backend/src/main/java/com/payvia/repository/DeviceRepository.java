package com.payvia.repository;

import com.payvia.entity.Device;
import com.payvia.entity.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByUserId(UUID userId);
    Optional<Device> findByIdAndUserId(UUID id, UUID userId);
    Optional<Device> findByIdAndUserIdAndStatus(UUID id, UUID userId, DeviceStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Device d WHERE d.id = :id AND d.user.id = :userId AND d.status = :status")
    Optional<Device> findByIdAndUserIdAndStatusForUpdate(@Param("id") UUID id, @Param("userId") UUID userId, @Param("status") DeviceStatus status);
}