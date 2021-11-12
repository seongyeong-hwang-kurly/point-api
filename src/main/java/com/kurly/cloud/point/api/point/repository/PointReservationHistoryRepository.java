package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.PointReservationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointReservationHistoryRepository extends JpaRepository<PointReservationHistoryEntity, Long> {
}
