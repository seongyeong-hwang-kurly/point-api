package com.kurly.cloud.point.api.point.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberPointHistoryDto {

  long seq;
  long orderNumber;
  long point;
  String detail;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String memo;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime regDateTime;
  long regTimestamp;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime expireDateTime;
  long expireTimestamp;

  /**
   * Entity 를 Dto로 변환한다.
   */
  public static MemberPointHistoryDto fromEntity(MemberPointHistory memberPointHistory,
                                                 boolean includeMemo) {
    return MemberPointHistoryDto.builder()
        .seq(memberPointHistory.getSeq())
        .orderNumber(memberPointHistory.getOrderNumber())
        .point(memberPointHistory.getTotalPoint())
        .detail(memberPointHistory.getDetail())
        .memo(includeMemo ? memberPointHistory.getMemo() : null)
        .regDateTime(DateTimeUtil.toZonedDateTime(memberPointHistory.getRegTime()))
        .regTimestamp(DateTimeUtil.toTimestamp(memberPointHistory.getRegTime()))
        .expireDateTime(DateTimeUtil.toZonedDateTime(memberPointHistory.getExpireTime()))
        .expireTimestamp(DateTimeUtil.toTimestamp(memberPointHistory.getExpireTime()))
        .build();
  }
}
