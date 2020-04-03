/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

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
  int totalPoint;

  @Column(name = "free_point")
  int freePoint;

  @Column(name = "cash_point")
  int cashPoint;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "update_time")
  LocalDateTime updateTime;

  @Transient
  public void plusPoint(int freePoint, int cashPoint) {
    setTotalPoint(getTotalPoint() + freePoint + cashPoint);
    setFreePoint(getFreePoint() + freePoint);
    setCashPoint(getCashPoint() + cashPoint);
    setUpdateTime(LocalDateTime.now());
  }

  @Transient
  public void minusPoint(int freePoint, int cashPoint) {
    setTotalPoint(getTotalPoint() - freePoint - cashPoint);
    setFreePoint(getFreePoint() - freePoint);
    setCashPoint(getCashPoint() - cashPoint);
    setUpdateTime(LocalDateTime.now());
  }

  @Transient
  public int getRepayAmount(int publishedAmount) {
    if (getTotalPoint() >= publishedAmount) return 0;

    int repayAmount;
    if (getTotalPoint() < 0) { // 아직도 빚이 남아있는 상태
      repayAmount = publishedAmount;
    } else {
      repayAmount = publishedAmount - getTotalPoint();
    }

    return repayAmount;
  }
}
