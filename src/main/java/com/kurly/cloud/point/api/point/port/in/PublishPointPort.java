package com.kurly.cloud.point.api.point.port.in;

import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;

public interface PublishPointPort {
  /**
   * 적립금을 요청한 사용자에게 발급(지급) 합니다.
   *
   * @param request request
   */
  Point publish(PublishPointRequest request);

  /**
   * 주문 적립 적립금을 사용자에게 발급(지급) 합니다.
   *
   * @param request request
   */
  Point publishByOrder(PublishPointRequest request) throws AlreadyPublishedException;

  /**
   * 발급 된 주문 적립 적립금을 취소합니다 <br/>
   * 만약 회원이 이미 적립금을 써버렸다면 대출(마이너스)이 발생됩니다.
   *
   * @param request request
   */
  void cancelPublishByOrder(CancelPublishOrderPointRequest request);
}
