package com.kurly.cloud.point.api.point.port.out;

import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.OrderPublishedNotFoundException;

public interface OrderPointPort {
  Point getOrderPublished(long orderNumber) throws OrderPublishedNotFoundException;
}
