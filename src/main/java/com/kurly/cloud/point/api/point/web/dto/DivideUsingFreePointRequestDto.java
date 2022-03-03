package com.kurly.cloud.point.api.point.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "create")
public class DivideUsingFreePointRequestDto {
    private int totalDealProductPrice;
    private int usingFreePoint;
    private int usingPaidPoint;
    private List<DealProductRequestDto> dealProducts;


}
