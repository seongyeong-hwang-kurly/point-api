package com.kurly.cloud.point.api.point.service.port.in;

import com.kurly.cloud.point.api.point.domain.CancelPublishOrderPointRequest;
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

  /**
   * 발급 된 주문 적립 포인트를 취소합니다 <br/>
   * 만약 회원이 이미 포인트를 써버렸다면 대출(마이너스)이 발생됩니다 <br/>
   *
   * @param request request
   */
  void cancelPublishByOrder(CancelPublishOrderPointRequest request);
}
