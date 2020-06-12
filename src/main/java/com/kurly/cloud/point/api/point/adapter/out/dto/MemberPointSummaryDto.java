package com.kurly.cloud.point.api.point.adapter.out.dto;

import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
public class MemberPointSummaryDto {
  int amount;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime nextExpireDate;
  long nextExpireTimestamp;
  int nextExpireAmount;

  /**
   * Entity 를 Dto로 변환한다.
   */
  public static MemberPointSummaryDto fromSummary(MemberPointSummary memberPointSummary) {
    return MemberPointSummaryDto.builder()
        .amount(memberPointSummary.getAmount())
        .nextExpireDate(DateTimeUtil.toZonedDateTime(memberPointSummary.getNextExpireDate()))
        .nextExpireTimestamp(Timestamp.valueOf(memberPointSummary.getNextExpireDate()).getTime())
        .nextExpireAmount(memberPointSummary.getNextExpireAmount())
        .build();
  }
}
