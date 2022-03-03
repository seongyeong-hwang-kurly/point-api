package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.api.common.domain.ApiResponseModel;
import com.kurly.cloud.point.api.point.service.DivideUsingFreePointService;

import com.kurly.cloud.point.api.point.web.dto.DealProductResponseDto;
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointRequestDto;
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DivideUsingFreePointController {


    @PostMapping("/divide")
    ApiResponseModel<DivideUsingFreePointResponseDto> divide(
            @RequestBody DivideUsingFreePointRequestDto divideUsingFreePointRequestDto){
        List<DealProductResponseDto> result = DivideUsingFreePointService.divide(divideUsingFreePointRequestDto);
        return new ApiResponseModel(true, "success", DivideUsingFreePointResponseDto.create(result));
    }

}
