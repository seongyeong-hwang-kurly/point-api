package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import com.kurly.cloud.point.api.point.util.VersionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 포인트 발급에 Timeout 을 적용한 구현체.
 */
@Service
@RequiredArgsConstructor
public class PublishPointServiceV2 implements PublishPointUseCase {

  private final PublishPointUseCase publishPointUseCase;

  @Transactional(timeout = VersionUtil.V2_TIMEOUT_SECONDS)
  @Override
  public Point publish(PublishPointRequest request) {
    return publishPointUseCase.publish(request);
  }

  @Transactional(timeout = VersionUtil.V2_TIMEOUT_SECONDS)
  @Override
  public Point publishByOrder(PublishPointRequest request)
      throws AlreadyPublishedException {
    return publishPointUseCase.publishByOrder(request);
  }

  @Transactional(timeout = VersionUtil.V2_TIMEOUT_SECONDS)
  @Override
  public void cancelPublishByOrder(CancelPublishOrderPointRequest request) {
    publishPointUseCase.cancelPublishByOrder(request);
  }
}
