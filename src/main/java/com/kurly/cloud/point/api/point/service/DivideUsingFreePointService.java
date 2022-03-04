package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.web.dto.DealProductResponseDto;
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointRequestDto;
import com.kurly.cloud.point.api.point.service.helper.DivideUsingFreePointHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DivideUsingFreePointService {
    public static List<DealProductResponseDto> divide(DivideUsingFreePointRequestDto param) {
        int totalUsingFreePoint = param.getUsingFreePoint();
        List<DealProductResponseDto> responseParams = DivideUsingFreePointHelper.makeResponse(param) ;

        int usedFreePointSum = DivideUsingFreePointHelper.getUsedFreePointSum(responseParams);
        int subPoint = totalUsingFreePoint - usedFreePointSum;

        responseParams.stream().limit(subPoint).
                forEach(DealProductResponseDto::increaseUseFreePoint);

        int totalUsedFreePoint = responseParams.stream()
                .mapToInt(DealProductResponseDto::getUsedFreePoint).sum();
        if (totalUsingFreePoint != totalUsedFreePoint) throw new IllegalArgumentException("요청한 usingFreePoint와 계산된 무상적립금이 일치하지 않습니다.");

        return responseParams;
    }

}
