package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointReservationRepository extends JpaRepository<PointReservationEntity, Long> {
    List<PointReservationEntity> findAllByMemberNumberAndAppliedIsFalse(long memberNumber);
}
