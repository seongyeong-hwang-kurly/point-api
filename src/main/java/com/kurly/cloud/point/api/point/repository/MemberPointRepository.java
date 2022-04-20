package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberPointRepository extends JpaRepository<MemberPoint, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MemberPoint> findWithPessimisticLockById(Long aLong);

    List<MemberPoint> findAllByMemberNumber(long memberNumber);
}
