package com.kurly.cloud.point.api.point.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.kurly.cloud.point.api.point.domain.reserve.ProductReserveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PointReserveCalculator class")
class PointReserveCalculatorTest {

  @Nested
  @DisplayName("calculate method")
  class DescribeCalculate {

    int subject(ProductReserveType reserveType,
                int productReserveValue,
                int payPrice,
                float memberPointReserveRatio) {
      return PointReserveCalculator.calculate(reserveType,
          productReserveValue,
          payPrice,
          memberPointReserveRatio);
    }

    int givenPayPrice() {
      return 10000;
    }

    float givenMemberPointReserveRatio() {
      return 3.0f;
    }

    @Nested
    @DisplayName("상품의 적립타입이 MEMBER(0)일 때")
    class ContextWithMemberType {

      ProductReserveType givenProductReserveType() {
        return ProductReserveType.MEMBER;
      }

      @Nested
      @DisplayName("상품의 적립값에 어떠한 값이 입력되어도")
      class ContextWithAnyValue {

        @ParameterizedTest
        @ValueSource(ints = {0, 1000, -100, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("회원의 적립률을 적용한 값을 리턴한다.")
        void test(int value) {
          var subject = subject(givenProductReserveType(),
              value,
              givenPayPrice(),
              givenMemberPointReserveRatio());

          var memberReserve =
              PointReserveCalculator.calculate(givenPayPrice(), givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(memberReserve);
        }
      }
    }

    @Nested
    @DisplayName("상품의 적립타입이 FIXED(1)일 때")
    class ContextWithFixedType {
      ProductReserveType givenProductReserveType() {
        return ProductReserveType.FIXED;
      }

      @Nested
      @DisplayName("상품의 적립값이 양수라면")
      class ContextWithPositiveValue {

        @DisplayName("적립값 그대로 리턴한다.")
        @ParameterizedTest
        @ValueSource(ints = {0, 1000, Integer.MAX_VALUE})
        void test(int value) {
          var subject = subject(givenProductReserveType(),
              value,
              givenPayPrice(),
              givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(value);
        }
      }

      @Nested
      @DisplayName("상품의 적립값이 음수라면")
      class ContextWithNegativeValue {

        @DisplayName("0을 리턴한다.")
        @ParameterizedTest
        @ValueSource(ints = {-100, Integer.MIN_VALUE})
        void test(int value) {
          var subject = subject(givenProductReserveType(),
              value,
              givenPayPrice(),
              givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(0);
        }
      }
    }

    @Nested
    @DisplayName("상품의 적립타입이 EXCLUDE(2)일 때")
    class ContextWithExcludeType {

      ProductReserveType givenProductReserveType() {
        return ProductReserveType.EXCLUDE;
      }

      @Nested
      @DisplayName("상품의 적립값에 어떠한 값이 입력되어도")
      class ContextWithAnyValue {

        @DisplayName("0을 리턴한다.")
        @ParameterizedTest
        @ValueSource(ints = {0, 1000, -100, Integer.MAX_VALUE, Integer.MIN_VALUE})
        void test(int value) {
          var subject = subject(givenProductReserveType(),
              value,
              givenPayPrice(),
              givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(0);
        }
      }
    }

    @Nested
    @DisplayName("상품의 적립타입이 PERCENT(3)일 때")
    class ContextWithPercentType {
      ProductReserveType givenProductReserveType() {
        return ProductReserveType.PERCENT;
      }

      @Nested
      @DisplayName("상품의 적립값이 0 이라면")
      class ContextWithEmptyValue {
        int givenProductValue() {
          return 0;
        }

        @Test
        @DisplayName("0을 리턴한다.")
        void test() {
          var subject = subject(givenProductReserveType(),
              givenProductValue(),
              givenPayPrice(),
              givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("상품의 적립값이 0 이상이라면")
      class ContextWithNonEmptyValue {

        int givenProductValue() {
          return 5;
        }

        @Test
        @DisplayName("상품의 적립률을 적용한 값을 리턴한다.")
        void test() {
          var subject = subject(givenProductReserveType(),
              givenProductValue(),
              givenPayPrice(),
              givenMemberPointReserveRatio());

          var productReserve =
              PointReserveCalculator.calculate(givenPayPrice(), givenProductValue());

          assertThat(subject)
              .isEqualTo(productReserve);
        }
      }
    }

    @Nested
    @DisplayName("상품의 적립타입이 MAX_PERCENT(4)일 때")
    class ContextWithMaxPercentType {
      ProductReserveType givenProductReserveType() {
        return ProductReserveType.MAX_PERCENT;
      }

      @Nested
      @DisplayName("상품의 적립값이 회원의 적립률보다 작다면")
      class ContextWithLessThenMemberValue {
        int givenProductValue() {
          return (int) (givenMemberPointReserveRatio() - 1);
        }

        @Test
        @DisplayName("상품의 적립률을 적용한 값을 리턴한다.")
        void test() {
          var subject = subject(givenProductReserveType(),
              givenProductValue(),
              givenPayPrice(),
              givenMemberPointReserveRatio());

          var productReserve =
              PointReserveCalculator.calculate(givenPayPrice(), givenProductValue());

          assertThat(subject)
              .isEqualTo(productReserve);
        }
      }

      @Nested
      @DisplayName("상품의 적립값이 회원의 적립률보다 크다면")
      class ContextWithGreaterThenMemberValue {

        int givenProductValue() {
          return (int) (givenMemberPointReserveRatio() + 1);
        }

        @Test
        @DisplayName("회원의 적립률을 적용한 값을 리턴한다.")
        void test() {
          var subject = subject(givenProductReserveType(),
              givenProductValue(),
              givenPayPrice(),
              givenMemberPointReserveRatio());

          var memberReserve =
              PointReserveCalculator.calculate(givenPayPrice(), givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(memberReserve);
        }
      }
    }

    @Nested
    @DisplayName("상품의 적립타입이 MAX_FIXED(5)일 때")
    class ContextWithMaxFixedType {
      ProductReserveType givenProductReserveType() {
        return ProductReserveType.MAX_FIXED;
      }

      @Nested
      @DisplayName("상품의 적립값이 회원의 적립률을 적용한 값보다 작다면")
      class ContextWithLessThenMemberCalculatedValue {
        int givenProductValue() {
          return PointReserveCalculator.calculate(givenPayPrice(), givenMemberPointReserveRatio())
              - 100;
        }

        @Test
        @DisplayName("상품의 적립값을 리턴한다.")
        void test() {
          var subject = subject(givenProductReserveType(),
              givenProductValue(),
              givenPayPrice(),
              givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(givenProductValue());
        }
      }

      @Nested
      @DisplayName("상품의 적립값이 회원의 적립률을 적용한 값보다 크다면")
      class ContextWithGreaterThenMemberCalculatedValue {

        int givenProductValue() {
          return PointReserveCalculator.calculate(givenPayPrice(), givenMemberPointReserveRatio())
              + 100;
        }

        @Test
        @DisplayName("회원의 적립률을 적용한 값을 리턴한다.")
        void test() {
          var subject = subject(givenProductReserveType(),
              givenProductValue(),
              givenPayPrice(),
              givenMemberPointReserveRatio());

          var memberReserve =
              PointReserveCalculator.calculate(givenPayPrice(), givenMemberPointReserveRatio());

          assertThat(subject)
              .isEqualTo(memberReserve);
        }
      }
    }
  }
}
