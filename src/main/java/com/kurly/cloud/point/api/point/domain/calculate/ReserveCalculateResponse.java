package com.kurly.cloud.point.api.point.domain.calculate;

import com.kurly.cloud.point.api.point.domain.reserve.ProductReserveType;
import java.util.List;
import lombok.Getter;

/**
 * 적립 예정 적립금 계산 응답.
 */
@Getter
public class ReserveCalculateResponse {

  int totalReserve;
  List<Item> products;

  /**
   * 적립 예정 적립금 계산 응답을 생성합니다.
   *
   * @param products     적립 예정 상품
   * @param totalReserve 총 적립 예정 적립금
   */
  public ReserveCalculateResponse(List<Item> products,
                                  int totalReserve) {
    this.totalReserve = totalReserve;
    this.products = products;
  }

  /**
   * 계산 된 적립 예정 상품.
   */
  @Getter
  public static class Item {
    int price;
    int quantity;
    int totalPrice;
    long contentProductNo;
    long dealProductNo;
    ProductReserveType reserveType;
    float reserveValue;
    int reserve;
    int totalReserve;

    /**
     * 계산 된 적립 예정 상품을 생성합니다.
     *
     * @param price            개별 가격
     * @param quantity         수량
     * @param totalPrice       총 가격 (개별가격 * 수량)
     * @param contentProductNo 컨텐츠 상품 번호
     * @param dealProductNo    딜 상품 번호
     * @param reserveType      적용된 적립 정책
     * @param reserveValue     적용된 적립 정책 값
     * @param reserve          적립 예정 적립금
     * @param totalReserve     총 적립 예정 적립금 (적립 예정 적립금 * 수량)
     */
    public Item(int price, int quantity, int totalPrice, long contentProductNo,
                long dealProductNo, ProductReserveType reserveType,
                float reserveValue,
                int reserve, int totalReserve) {
      this.price = price;
      this.quantity = quantity;
      this.totalPrice = totalPrice;
      this.contentProductNo = contentProductNo;
      this.dealProductNo = dealProductNo;
      this.reserveType = reserveType;
      this.reserveValue = reserveValue;
      this.reserve = reserve;
      this.totalReserve = totalReserve;
    }
  }
}
