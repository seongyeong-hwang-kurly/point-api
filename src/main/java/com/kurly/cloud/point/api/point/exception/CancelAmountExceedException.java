package com.kurly.cloud.point.api.point.exception;

import java.text.MessageFormat;

public class CancelAmountExceedException extends Exception {
  /**
   * 기본 생성자.
   */
  public CancelAmountExceedException(long orderNumber, int amount) {
    super(
        MessageFormat.format("사용 취소 적립금이 사용 한 적립금보다 많습니다.[{0} {1}]",
            String.valueOf(orderNumber), amount)
    );
  }
}
