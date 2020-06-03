/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.order.repository;

import com.kurly.cloud.point.api.order.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  @Query("SELECT DISTINCT o FROM Order o"
      + " JOIN o.orderDynamicColumns "
      + " WHERE o.memberNumber <> 0 "
      + " AND o.payPrice > 0 "
      + " AND o.orderStatus <> 0 "
      + " AND o.orderProcessCode IN (0, 21, 22, 71) "
      + " AND o.payDateTime BETWEEN :from AND :to ")
  List<Order> findAllPointPublishableOrder(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);
}
