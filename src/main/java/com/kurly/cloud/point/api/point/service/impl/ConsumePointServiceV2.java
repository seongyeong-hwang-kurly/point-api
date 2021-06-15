package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
import com.kurly.cloud.point.api.point.util.VersionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 사용에 Timeout 을 적용한 구현체.
 */
@Service
@RequiredArgsConstructor
public class ConsumePointServiceV2 implements ConsumePointUseCase {

  private final ConsumePointUseCase consumePointUseCase;

  @Transactional(timeout = VersionUtil.V2_TIMEOUT_SECONDS)
  @Override
  public PointConsumeResult consume(ConsumePointRequest request)
      throws NotEnoughPointException {
    return consumePointUseCase.consume(request);
  }

  @Transactional(timeout = VersionUtil.V2_TIMEOUT_SECONDS)
  @Override
  public PointConsumeResult consumeByOrder(OrderConsumePointRequest request)
      throws NotEnoughPointException {
    return consumePointUseCase.consumeByOrder(request);
  }

  @Transactional(timeout = VersionUtil.V2_TIMEOUT_SECONDS)
  @Override
  public void cancelConsumeByOrder(CancelOrderConsumePointRequest request)
      throws CancelAmountExceedException {
    consumePointUseCase.cancelConsumeByOrder(request);
  }
}
