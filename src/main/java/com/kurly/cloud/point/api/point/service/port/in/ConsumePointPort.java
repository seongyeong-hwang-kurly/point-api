package com.kurly.cloud.point.api.point.service.port.in;

import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
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

  /**
   * 주문시 사용한 포인트를 사용 취소 합니다.
   */
  void cancelConsumeByOrder(CancelOrderConsumePointRequest request) throws CancelAmountExceedException;
}
