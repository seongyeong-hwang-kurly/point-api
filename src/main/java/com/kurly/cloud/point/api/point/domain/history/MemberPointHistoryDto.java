/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.domain.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberPointHistoryDto {

  long seq;
  long orderNumber;
  int point;
  String detail;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String memo;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime regDateTime;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime expireDateTime;

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
        .regDateTime(memberPointHistory.getRegTime())
        .expireDateTime(memberPointHistory.getExpireTime())
        .build();
  }
}
