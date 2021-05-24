package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;

public interface ConsumePointUseCase {
  /**
   * 적립금을 사용합니다.
   *
   * @return result
   */
  PointConsumeResult consume(ConsumePointRequest request) throws NotEnoughPointException;

  /**
   * 주문시 적립금을 사용합니다.
   *
   * @return result
   */
  PointConsumeResult consumeByOrder(OrderConsumePointRequest request)
      throws NotEnoughPointException;

  /**
   * 주문시 사용한 적립금을 사용 취소 합니다.
   */
  void cancelConsumeByOrder(CancelOrderConsumePointRequest request)
      throws CancelAmountExceedException;
}
