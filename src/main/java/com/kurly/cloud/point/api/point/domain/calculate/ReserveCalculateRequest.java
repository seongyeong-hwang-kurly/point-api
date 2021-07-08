package com.kurly.cloud.point.api.point.domain.calculate;

import com.kurly.cloud.point.api.point.domain.reserve.ProductReserveType;
import java.util.List;
import lombok.Getter;

/**
 * 적립 예정 적립금 계산 요청.
 */
@Getter
public class ReserveCalculateRequest {

  float memberReserveRatio;
  List<Item> products;

  /**
   * 적립 예정 적립금 계산 요청을 생성합니다.
   *
   * @param memberReserveRatio 회원 혜택 적립률
   * @param products           적립 예정 상품
   */
  public ReserveCalculateRequest(float memberReserveRatio,
                                 List<Item> products) {
    this.memberReserveRatio = memberReserveRatio;
    this.products = products;
  }

  /**
   * 적립 예정 상품.
   */
  @Getter
  public static class Item {
    int price;
    int quantity;
    int totalPrice;
    long contentProductNo;
    long dealProductNo;
    ProductReserveType productReserveType;
    int productReserveValue;

    /**
     * 적립 예정 상품을 생성합니다.
     *
     * @param price               개별 가격
     * @param quantity            수량
     * @param totalPrice          총 가격 (개별가격 * 수량)
     * @param contentProductNo    컨텐츠 상품 번호
     * @param dealProductNo       딜 상품 번호
     * @param productReserveType  상품 적립 정책
     * @param productReserveValue 상품 적립 값
     */
    public Item(int price, int quantity, int totalPrice, long contentProductNo,
                long dealProductNo,
                ProductReserveType productReserveType, int productReserveValue) {
      this.price = price;
      this.quantity = quantity;
      this.totalPrice = totalPrice;
      this.contentProductNo = contentProductNo;
      this.dealProductNo = dealProductNo;
      this.productReserveType = productReserveType;
      this.productReserveValue = productReserveValue;
    }
  }
}
