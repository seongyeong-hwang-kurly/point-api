package com.kurly.cloud.point.api.point.domain.consume;

import lombok.Getter;

@Getter
public class ConsumedPoint {
  long pointSeq;
  long consumed;
  boolean settle;

  /**
   * 기본 생성자.
   */
  public ConsumedPoint(long pointSeq, long consumed, boolean settle) {
    this.pointSeq = pointSeq;
    this.consumed = consumed;
    this.settle = settle;
  }
}
