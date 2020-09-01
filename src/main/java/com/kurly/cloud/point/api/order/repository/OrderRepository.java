package com.kurly.cloud.point.api.order.repository;

import com.kurly.cloud.point.api.order.entity.Order;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

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
