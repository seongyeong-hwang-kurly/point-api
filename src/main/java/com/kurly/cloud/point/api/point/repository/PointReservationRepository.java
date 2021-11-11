package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.PointReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointReservationRepository extends JpaRepository<PointReservation, Long> {
    List<PointReservation> findAllByMemberNumber(long memberNumber);
}
