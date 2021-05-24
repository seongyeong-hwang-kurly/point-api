package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.OrderPublishedNotFoundException;

public interface OrderPointUseCase {
  Point getOrderPublished(long orderNumber) throws OrderPublishedNotFoundException;
}
