package com.kurly.cloud.point.api.point.service.port.in;

import com.kurly.cloud.point.api.point.domain.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.PointConsumeResult;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;

public interface ConsumePointPort {
  /**
   * 포인트를 사용합니다.
   *
   * @return result
   */
  PointConsumeResult consume(ConsumePointRequest request) throws NotEnoughPointException;

  /**
   * 주문시 포인트를 사용합니다.
   *
   * @return result
   */
  PointConsumeResult consumeByOrder(OrderConsumePointRequest request) throws NotEnoughPointException;
}
