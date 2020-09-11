package com.kurly.cloud.point.api.point.entity;

import com.kurly.cloud.point.api.point.entity.converter.UnixTimestampConverter;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mk_point_info")
public class MemberPoint {
  @Id
  @Column(name = "m_no")
  long memberNumber;

  @Column(name = "total_point")
  long totalPoint;

  @Column(name = "free_point")
  long freePoint;

  @Column(name = "cash_point")
  long cashPoint;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "update_time")
  LocalDateTime updateTime;

  /**
   * 적립금을 더함.
   */
  @Transient
  public void plusPoint(long freePoint, long cashPoint) {
    setTotalPoint(getTotalPoint() + freePoint + cashPoint);
    setFreePoint(getFreePoint() + freePoint);
    setCashPoint(getCashPoint() + cashPoint);
    setUpdateTime(LocalDateTime.now());
  }

  /**
   * 적립금을 뺌.
   */
  @Transient
  public void minusPoint(long freePoint, long cashPoint) {
    setTotalPoint(getTotalPoint() - freePoint - cashPoint);
    setFreePoint(getFreePoint() - freePoint);
    setCashPoint(getCashPoint() - cashPoint);
    setUpdateTime(LocalDateTime.now());
  }

  /**
   * 상환할 빚이 있는지 계산 한다.
   */
  @Transient
  public long getRepayAmount(long publishedAmount) {
    if (getTotalPoint() >= publishedAmount) {
      return 0;
    }

    long repayAmount;
    if (getTotalPoint() < 0) { // 아직도 빚이 남아있는 상태
      repayAmount = publishedAmount;
    } else {
      repayAmount = publishedAmount - getTotalPoint();
    }

    return repayAmount;
  }

  /**
   * 적립금을 사용하기에 충분히 가지고 있는지 계산한다.
   */
  @Transient
  public boolean isEnough(Long point, boolean settle) {
    return settle ? getCashPoint() >= point : getTotalPoint() >= point;
  }
}
