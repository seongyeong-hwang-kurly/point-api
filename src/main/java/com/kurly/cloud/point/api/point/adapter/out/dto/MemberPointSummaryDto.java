package com.kurly.cloud.point.api.point.adapter.out.dto;

import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
public class MemberPointSummaryDto {
  long amount;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime nextExpireDate;
  long nextExpireTimestamp;
  long nextExpireAmount;

  /**
   * Entity 를 Dto로 변환한다.
   */
  public static MemberPointSummaryDto fromSummary(MemberPointSummary memberPointSummary) {
    return MemberPointSummaryDto.builder()
        .amount(memberPointSummary.getAmount())
        .nextExpireDate(DateTimeUtil.toZonedDateTime(memberPointSummary.getNextExpireDate()))
        .nextExpireTimestamp(DateTimeUtil.toTimestamp(memberPointSummary.getNextExpireDate()))
        .nextExpireAmount(memberPointSummary.getNextExpireAmount())
        .build();
  }
}
