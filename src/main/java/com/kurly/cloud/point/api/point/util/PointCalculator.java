package com.kurly.cloud.point.api.point.util;

public class PointCalculator {
  /**
   * 적립률에 따른 발생 적립금을 계산한다.
   */
  public static int calculateOrderPoint(int payPrice, float pointRatio) {
    if (pointRatio == 0) {
      return 0;
    }
    int point = Math.round(payPrice * pointRatio / 100);
    return point == 0 ? 1 : point;
  }
}
