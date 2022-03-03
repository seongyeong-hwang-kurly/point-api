package com.kurly.cloud.point.api.point.service.helper;

import com.kurly.cloud.point.api.point.param.DealProductRequestParam;
import com.kurly.cloud.point.api.point.param.DealProductResponseParam;
import com.kurly.cloud.point.api.point.param.DivideUsingFreePointRequestParam;

import java.util.List;
import java.util.stream.Collectors;

public class DivideUsingFreePointHelper {

    public static List<DealProductResponseParam> makeResponse(DivideUsingFreePointRequestParam param){
        return    param.getDealProducts().stream()
                .map(deal -> createEachResponse(param, deal))
                .collect(Collectors.toList());
    }

    private static DealProductResponseParam createEachResponse(DivideUsingFreePointRequestParam param, DealProductRequestParam sub) {
        int usedFreePoint = getProportionalPoint(
               param.getTotalDealProductPrice(),
                sub.getSellingPrice(),
                param.getUsingFreePoint()
        );
        return DealProductResponseParam.create(
                sub.getDealProductNo(),
                usedFreePoint,
                0);
    }

    public static int getProportionalPoint(int totalPrice, int eachPrice, int point) {
        return (int) (point * ((double)eachPrice/totalPrice));
    }

    public static int getUsedFreePointSum(List<DealProductResponseParam> responseParams) {
        return responseParams.stream().mapToInt(DealProductResponseParam::getUsedFreePoint).sum();
    }

}
