package com.kurly.cloud.point.api.point.exception;

import java.text.MessageFormat;

public class AlreadyPublishedException extends Exception {
  public AlreadyPublishedException(long orderNumber) {
    super(MessageFormat.format("이미 적립된 주문번호입니다.[{0}] ", orderNumber));
  }
}
