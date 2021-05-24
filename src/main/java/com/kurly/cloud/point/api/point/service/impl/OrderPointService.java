package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.OrderPublishedNotFoundException;
import com.kurly.cloud.point.api.point.service.OrderPointUseCase;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderPointService implements OrderPointUseCase {

  private final PointDomainService pointDomainService;

  @Transactional(readOnly = true)
  @Override
  public Point getOrderPublished(long orderNumber)
      throws OrderPublishedNotFoundException {
    Optional<Point> published = pointDomainService.getPublishedByOrderNumber(orderNumber);
    if (published.isEmpty()) {
      throw new OrderPublishedNotFoundException(orderNumber);
    }
    return published.get();
  }
}
