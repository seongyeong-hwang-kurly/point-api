package com.kurly.cloud.point.api.point.exception;

import java.text.MessageFormat;

public class HistoryTypeNotFoundException extends Exception {
  public HistoryTypeNotFoundException(int historyType) {
    super(MessageFormat.format("존재하지 않는 이력 번호 입니다.[{0}] ", String.valueOf(historyType)));
  }
}
