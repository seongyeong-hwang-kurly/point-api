package com.kurly.cloud.point.api.point.param;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(staticName = "create")
public class DivideUsingFreePointRequestParam {
    private int totalDealProductPrice;
    private int usingFreePoint;
    private int usingPaidPoint;
    private List<DealProductRequestParam> dealProducts;


}
