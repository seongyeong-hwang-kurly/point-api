package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.Point;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
  @Query("SELECT p FROM Point p WHERE "
      + " p.memberNumber = :memberNumber AND p.remain > 0 AND "
      + " (p.expireTime = 0L OR p.expireTime >= :expireTime) ")
  List<Point> findAllAvailableMemberPoint(
      @Param("memberNumber") long memberNumber,
      @Param("expireTime") LocalDateTime expireTime);

  @Query("SELECT p FROM Point p WHERE "
      + " p.memberNumber = :memberNumber AND p.remain > 0 AND "
      + " p.settle = true AND "
      + " (p.expireTime = 0L OR p.expireTime >= :expireTime) ")
  List<Point> findAllAvailableSettleMemberPoint(
      @Param("memberNumber") long memberNumber,
      @Param("expireTime") LocalDateTime expireTime);

  @Query("SELECT p FROM Point p WHERE " +
      " p.memberNumber = :memberNumber AND p.orderNumber = :orderNumber AND " +
      " p.historyType = 1 AND p.remain > 0")
  Optional<Point> findAvailableOrderPublishedPoint(@Param("memberNumber") long memberNumber,
                                                   @Param("orderNumber") long orderNumber);

  @Query("SELECT p FROM Point p WHERE p.memberNumber = :memberNumber AND p.remain < 0")
  List<Point> findAllDebtMemberPoint(@Param("memberNumber") long memberNumber);

  @Query("SELECT p from Point p WHERE "
      + " p.memberNumber = :memberNumber AND p.remain > 0 AND "
      + " p.expireTime <= :expireTime AND p.settle = false AND p.payment = false "
  )
  List<Point> findAllExpiredMemberPoint(
      @Param("memberNumber") long memberNumber,
      @Param("expireTime") LocalDateTime expireTime
  );

  @Query("SELECT DISTINCT p.memberNumber FROM Point p "
      + " WHERE p.payment = false AND p.settle = false AND p.remain > 0 "
      + " AND p.expireTime <= :expireTime ")
  List<Long> findAllMemberNumberHasExpiredPoint(
      @Param("expireTime") LocalDateTime expireTime,
      Pageable pageable);

  @Query("SELECT p.expireTime FROM Point p "
      + " WHERE p.memberNumber = :memberNumber AND p.remain > 0 "
      + " AND p.payment = false AND p.settle = false "
      + " ORDER BY p.expireTime ASC ")
  Page<LocalDateTime> getMemberNextExpireTime(@Param("memberNumber") long memberNumber,
                                              Pageable pageable);

  Optional<Point> findByOrderNumberAndHistoryType(long orderNumber, int historyType);
}
