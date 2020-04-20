package com.kurly.cloud.point.api.point.util;

public class PointCalculator {
  public static int calculateOrderPoint(int payPrice, float pointRatio) {
    if (pointRatio == 0) return 0;
    return Math.round(payPrice * pointRatio / 100);
  }
}
