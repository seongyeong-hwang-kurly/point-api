package com.kurly.cloud.point.api.point.exception;

import java.text.MessageFormat;

public class WrongPointRequestException extends RuntimeException {
  public WrongPointRequestException(long requested, long consumed) {
    super(MessageFormat.format("요청한 적립금이 사용된 적립금보다 큽니다.[기사용 : {0}, 요청 : {1}]", consumed, requested));
  }
}
