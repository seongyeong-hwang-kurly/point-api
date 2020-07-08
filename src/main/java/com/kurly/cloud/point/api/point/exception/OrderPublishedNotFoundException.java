package com.kurly.cloud.point.api.point.exception;

import java.text.MessageFormat;

public class OrderPublishedNotFoundException extends Exception {
  public OrderPublishedNotFoundException(long orderNumber) {
    super(MessageFormat.format("해당 주문으로 적립 된 내역이 없습니다 [{0}]", String.valueOf(orderNumber)));
  }
}
