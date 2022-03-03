package com.kurly.cloud.point.api.point.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.val;

import java.util.List;

@Getter
@Builder
public class DivideUsingFreePointRequestDTO {
    Long totalDealProductPrice;
    Integer usingFreePoint;
    Integer usingPaidPoint;
    List<DealProductDto> dealProducts;
}
