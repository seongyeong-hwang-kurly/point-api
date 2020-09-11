package com.kurly.cloud.point.api.point.exception;

import java.text.MessageFormat;

public class NotEnoughPointException extends Exception {
  public NotEnoughPointException(long requested, long has) {
    super(MessageFormat.format("사용 가능한 적립금이 부족합니다.[가능 : {0}, 요청 : {1}]", has, requested));
  }
}
