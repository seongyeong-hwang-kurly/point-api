package com.kurly.cloud.point.api.point.service.helper;

import com.kurly.cloud.point.api.point.web.dto.DealProductRequestDto;
import com.kurly.cloud.point.api.point.web.dto.DealProductResponseDto;
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointRequestDto;

import java.util.List;
import java.util.stream.Collectors;

public class DivideUsingFreePointHelper {

    public static List<DealProductResponseDto> makeResponse(DivideUsingFreePointRequestDto param){
        return    param.getDealProducts().stream()
                .map(deal -> createEachResponse(param, deal))
                .collect(Collectors.toList());
    }

    private static DealProductResponseDto createEachResponse(DivideUsingFreePointRequestDto param, DealProductRequestDto sub) {
        int usedFreePoint = getProportionalPoint(
               param.getTotalDealProductPrice(),
                sub.getSellingPrice(),
                param.getUsingFreePoint()
        );
        return DealProductResponseDto.create(
                sub.getDealProductNo(),
                usedFreePoint,
                0);
    }

    public static int getProportionalPoint(int totalPrice, int eachPrice, int point) {
        return (int) (point * ((double)eachPrice/totalPrice));
    }

    public static int getUsedFreePointSum(List<DealProductResponseDto> responseDtos) {
        return responseDtos.stream().mapToInt(DealProductResponseDto::getUsedFreePoint).sum();
    }

}
