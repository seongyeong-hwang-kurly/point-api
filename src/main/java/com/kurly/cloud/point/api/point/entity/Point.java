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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mk_point_list",
    indexes = {
        @Index(columnList = "m_no")
        , @Index(columnList = "ordno")
        , @Index(columnList = "point_type")
        , @Index(columnList = "reg_time")
        , @Index(columnList = "expire_time")
    }
)
public class Point {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long seq;

  @NotNull
  @Column(name = "m_no")
  Long memberNumber;

  @Column(name = "ordno")
  long orderNumber;

  @NotNull
  @Column(name = "charge")
  Integer charge;

  @NotNull
  @Column(name = "remain")
  Integer remain;

  @Column(name = "point_ratio")
  float pointRatio;

  @NotNull
  @Column(name = "point_type")
  Integer historyType;

  @Column(name = "refund_type")
  int refundType;

  @Type(type = "numeric_boolean")
  @Column(name = "is_payment")
  boolean payment;

  @Type(type = "numeric_boolean")
  @Column(name = "settle_flag")
  boolean settle;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "reg_time")
  LocalDateTime regTime;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "expire_time")
  LocalDateTime expireTime;

  @Transient
  public void expire() {
    this.remain = 0;
  }
}
