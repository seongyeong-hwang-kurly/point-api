/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.Point;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {
  @Query("SELECT p FROM Point p WHERE " +
      "p.memberNumber = :memberNumber AND p.remain > 0 AND" +
      "(p.expireTime = 0L OR p.expireTime >= :expireTime)")
  List<Point> findAllAvailableMemberPoint(
      @Param("memberNumber") long memberNumber,
      @Param("expireTime") LocalDateTime expireTime);
}
