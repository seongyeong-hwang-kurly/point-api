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
      + " WHERE o.memberNumber <> 0 "
      + " AND o.publishPoint > 0 "
      + " AND o.orderStatus <> 0 "
      + " AND o.orderProcessCode IN (0, 21, 22, 71) "
      + " AND o.payDateTime BETWEEN :from AND :to ")
  List<Order> findAllPointPublishableOrder(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);

  @Query("SELECT COUNT(o) FROM Order o" +
      " WHERE o.memberNumber = :memberNumber " +
      " AND o.orderStatus = 4" +
      " AND o.orderProcessCode IN (0, 21, 22)")
  int countMemberDeliveredOrder(@Param("memberNumber") long memberNumber);

  @Query("SELECT o FROM Order o" +
      " WHERE o.memberNumber = :memberNumber " +
      " AND o.orderStatus IN (1, 4) " +
      " AND o.orderProcessCode IN (0, 21, 22) " +
      " ORDER BY o.orderNumber DESC")
  List<Order> findMemberOrderedOrder(@Param("memberNumber") long memberNumber,
                                     Pageable pageable);

}
