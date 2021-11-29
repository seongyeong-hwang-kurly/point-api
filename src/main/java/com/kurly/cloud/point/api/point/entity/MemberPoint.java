package com.kurly.cloud.point.api.point.entity;

import com.kurly.cloud.point.api.point.entity.converter.UnixTimestampConverter;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@DynamicUpdate
@OptimisticLocking(type = OptimisticLockType.DIRTY)
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

  @Column(name = "expired_at")
  LocalDateTime expiredAt;

  public void plusPoint(long freePoint, long cashPoint) {
    setTotalPoint(getTotalPoint() + freePoint + cashPoint);
    setFreePoint(getFreePoint() + freePoint);
    setCashPoint(getCashPoint() + cashPoint);
    setUpdateTime(LocalDateTime.now());
  }

  public void minusPoint(long freePoint, long cashPoint) {
    setTotalPoint(getTotalPoint() - freePoint - cashPoint);
    setFreePoint(getFreePoint() - freePoint);
    setCashPoint(getCashPoint() - cashPoint);
    setUpdateTime(LocalDateTime.now());
  }

  public void expire(long freePoint, long cashPoint, LocalDateTime expiredAt){
    minusPoint(freePoint, cashPoint);
    Optional.ofNullable(expiredAt)
            .ifPresent(it ->
                    setExpiredAt(PointExpireDateCalculator.withEndOfDate(it)));
  }

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

  public boolean isEnough(Long point, boolean settle) {
    return settle ? getCashPoint() >= point : getTotalPoint() >= point;
  }
}
