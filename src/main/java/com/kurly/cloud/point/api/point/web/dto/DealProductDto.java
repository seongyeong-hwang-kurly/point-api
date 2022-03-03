package com.kurly.cloud.point.api.point.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DealProductDto {
    Long dealProductNo;
    Long sellingPrice;

}
