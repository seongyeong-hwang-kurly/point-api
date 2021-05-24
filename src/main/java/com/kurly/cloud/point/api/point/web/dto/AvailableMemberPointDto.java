package com.kurly.cloud.point.api.point.web.dto;

import com.kurly.cloud.point.api.point.entity.MemberPoint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableMemberPointDto {

  long total;
  long free;
  long cash;

  /**
   * Entity 를 Dto로 변환한다.
   */
  public static AvailableMemberPointDto fromEntity(MemberPoint memberPoint) {
    return AvailableMemberPointDto.builder()
        .total(memberPoint.getTotalPoint())
        .free(memberPoint.getFreePoint())
        .cash(memberPoint.getCashPoint())
        .build();
  }
}
