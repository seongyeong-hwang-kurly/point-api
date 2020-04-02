package com.kurly.cloud.point.api.point.service.port.in;

import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;

public interface PublishPointPort {
  /**
   * 포인트를 요청한 사용자에게 발급(지급) 합니다.
   *
   * @param request request
   */
  void publish(PublishPointRequest request);

  /**
   * 주문 적립 포인트를 사용자에게 발급(지급) 합니다.
   *
   * @param request request
   */
  void publishByOrder(PublishPointRequest request) throws AlreadyPublishedException;
}
