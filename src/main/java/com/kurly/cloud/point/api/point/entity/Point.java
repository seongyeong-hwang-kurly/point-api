package com.kurly.cloud.point.api.point.entity;

import com.kurly.cloud.point.api.point.entity.converter.UnixTimestampConverter;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mk_point_list",
    indexes = {
        @Index(columnList = "m_no"),
        @Index(columnList = "ordno"),
        @Index(columnList = "point_type"),
        @Index(columnList = "reg_time"),
        @Index(columnList = "expire_time")
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
  Long charge;

  @NotNull
  @Column(name = "remain")
  Long remain;

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

  @Column(name = "expired_at")
  LocalDateTime expiredAt;

  public void expire() {
    this.remain = 0L;
    this.expiredAt = expireTime;
  }
}
