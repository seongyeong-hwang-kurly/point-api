package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.param.DealProductResponseParam;
import com.kurly.cloud.point.api.point.param.DivideUsingFreePointRequestParam;
import com.kurly.cloud.point.api.point.service.helper.DivideUsingFreePointHelper;

import java.util.List;
import java.util.stream.Collectors;

public class DivideUsingFreePointService {
    public static List<DealProductResponseParam> divide(DivideUsingFreePointRequestParam param) {
        int targetPoint = param.getUsingFreePoint();
        List<DealProductResponseParam> responseParams = DivideUsingFreePointHelper.makeResponse(param) ;

        int usedFreePointSum = DivideUsingFreePointHelper.getUsedFreePointSum(responseParams);
        int subPoint = targetPoint - usedFreePointSum;

        responseParams.stream().limit(subPoint).
                forEach(DealProductResponseParam::increaseUseFreePoint);

        return responseParams;
    }




}
