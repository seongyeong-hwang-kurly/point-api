package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.OrderPublishedNotFoundException;
import com.kurly.cloud.point.api.point.port.out.OrderPointPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPointAdapter implements OrderPointPort {

  private final PointService pointService;

  @Transactional(readOnly = true)
  @Override public Point getOrderPublished(long orderNumber)
      throws OrderPublishedNotFoundException {
    Optional<Point> published = pointService.getPublishedByOrderNumber(orderNumber);
    if (published.isEmpty()) {
      throw new OrderPublishedNotFoundException(orderNumber);
    }
    return published.get();
  }
}
