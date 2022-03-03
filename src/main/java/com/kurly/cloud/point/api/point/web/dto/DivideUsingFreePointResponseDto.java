package com.kurly.cloud.point.api.point.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "create")
public class DivideUsingFreePointResponseDto {
    private List<DealProductResponseDto> dealProducts;
}
