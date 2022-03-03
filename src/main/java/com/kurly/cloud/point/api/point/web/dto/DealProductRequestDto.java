package com.kurly.cloud.point.api.point.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "create")
public class DealProductRequestDto {
    private Long dealProductNo;
    private int sellingPrice; // coupon 할인까지 적용된 상품별 금액
}
