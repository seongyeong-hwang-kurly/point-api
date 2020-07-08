package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPointRepository extends JpaRepository<MemberPoint, Long> {
}
