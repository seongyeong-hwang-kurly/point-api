package com.kurly.cloud.point.api.point.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateRequest;
import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateResponse;
import com.kurly.cloud.point.api.point.domain.reserve.ProductReserveType;
import com.kurly.cloud.point.api.point.util.PointReserveCalculator;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CalculateReserveService class")
class CalculateReserveServiceTest {

  CalculateReserveService calculateReserveService = new CalculateReserveService();

  @Nested
  @DisplayName("calculate method")
  class DescribeCalculate {
    ReserveCalculateResponse subject() {
      return calculateReserveService.calculate(givenRequest());
    }

    ReserveCalculateRequest givenRequest() {
      return new ReserveCalculateRequest(3, Collections.singletonList(givenRequestProduct()));
    }

    ReserveCalculateRequest.Item givenRequestProduct() {
      return new ReserveCalculateRequest.Item(1000, 2, 2000,
          1, 2, ProductReserveType.MEMBER, 0);
    }

    @Nested
    @DisplayName("적립 예정 적립금 계산을 요청하면")
    class ContextWithRequest {

      int expectReserve() {
        return PointReserveCalculator.calculate(1000, 3);
      }

      @DisplayName("요청한 내용과 함께 적립 예정 적립금을 리턴한다")
      @Test
      void test() {
        ReserveCalculateResponse subject = subject();
        ReserveCalculateResponse.Item
            item = subject.getProducts().get(0);
        ReserveCalculateRequest.Item givenProduct = givenRequestProduct();

        assertThat(subject.getTotalReserve())
            .isEqualTo(expectReserve() * givenRequestProduct().getQuantity());
        assertThat(item.getContentProductNo())
            .isEqualTo(givenProduct.getContentProductNo());
        assertThat(item.getDealProductNo())
            .isEqualTo(givenProduct.getDealProductNo());
        assertThat(item.getPrice())
            .isEqualTo(givenProduct.getPrice());
        assertThat(item.getQuantity())
            .isEqualTo(givenProduct.getQuantity());
        assertThat(item.getTotalPrice())
            .isEqualTo(givenProduct.getTotalPrice());
        assertThat(item.getReserve())
            .isEqualTo(expectReserve());
        assertThat(item.getTotalReserve())
            .isEqualTo(expectReserve() * item.getQuantity());
        assertThat(item.getReserveType())
            .isEqualTo(ProductReserveType.MEMBER);
        assertThat(item.getReserveValue())
            .isEqualTo(givenRequest().getMemberReserveRatio());
      }
    }
  }

}