package com.kurly.cloud.point.api.point.domain.history;

import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberPointHistoryInsertRequest {

  private long cashPoint;
  private long freePoint;
  private Integer type;
  private Long memberNumber;
  private long orderNumber;
  private boolean hidden;
  @Builder.Default
  private String detail = "";
  @Builder.Default
  private String memo = "";
  @Builder.Default
  private LocalDateTime regTime = LocalDateTime.now();
  private LocalDateTime expireTime;

  /**
   * Entity로 변환 한다.
   */
  public MemberPointHistory toEntity() {
    return MemberPointHistory.builder()
        .detail(this.detail)
        .cashPoint(this.cashPoint)
        .freePoint(this.freePoint)
        .totalPoint(getTotalPoint())
        .hidden(this.hidden)
        .historyType(this.type)
        .memberNumber(this.memberNumber)
        .memo(this.memo)
        .orderNumber(this.orderNumber)
        .expireTime(this.expireTime)
        .regTime(this.regTime)
        .build();
  }

  public long getTotalPoint() {
    return cashPoint + freePoint;
  }

}
