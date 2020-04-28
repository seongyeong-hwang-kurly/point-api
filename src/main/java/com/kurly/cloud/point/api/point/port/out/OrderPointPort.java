package com.kurly.cloud.point.api.point.port.out;

import com.kurly.cloud.point.api.point.entity.Point;
import java.util.Optional;

public interface OrderPointPort {
  Optional<Point> getOrderPublished(long orderNumber);
}
