package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.param.DealProductResponseParam;
import com.kurly.cloud.point.api.point.param.DivideUsingFreePointRequestParam;
import com.kurly.cloud.point.api.point.service.helper.DivideUsingFreePointHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DivideUsingFreePointService {
    public static List<DealProductResponseParam> divide(DivideUsingFreePointRequestParam param) {
        int totalUsingFreePoint = param.getUsingFreePoint();
        List<DealProductResponseParam> responseParams = DivideUsingFreePointHelper.makeResponse(param) ;

        int usedFreePointSum = DivideUsingFreePointHelper.getUsedFreePointSum(responseParams);
        int subPoint = totalUsingFreePoint - usedFreePointSum;

        responseParams.stream().limit(subPoint).
                forEach(DealProductResponseParam::increaseUseFreePoint);

        return responseParams;
    }




}
