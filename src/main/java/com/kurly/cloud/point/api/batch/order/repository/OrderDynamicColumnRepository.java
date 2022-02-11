package com.kurly.cloud.point.api.batch.order.repository;

import com.kurly.cloud.point.api.batch.order.entity.OrderDynamicColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDynamicColumnRepository extends JpaRepository<OrderDynamicColumn, Long> {
}
