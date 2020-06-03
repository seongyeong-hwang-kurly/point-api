package com.kurly.cloud.point.api.point.util;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PointCalculator class")
class PointCalculatorTest {
  @Nested
  @DisplayName("주문 적립률에 따른 적립금을 계산할때")
  class DescribeCalculateOrderPoint {
    @Nested
    @DisplayName("적립금액이 소수점 단위로 계산되면")
    class Context0 {

      @DisplayName("반올림 한 결과를 리턴한다")
      @Test
      void test() {
        assertThat(subject(1, 0.5f)).isEqualTo(1);
        assertThat(subject(350, 0.5f)).isEqualTo(2);
        assertThat(subject(460, 5)).isEqualTo(23);
        assertThat(subject(345, 7)).isEqualTo(24);
        assertThat(subject(350, 7)).isEqualTo(25);
      }

      int subject(int payPrice, float pointRatio) {
        return PointCalculator.calculateOrderPoint(payPrice, pointRatio);
      }
    }
  }

}