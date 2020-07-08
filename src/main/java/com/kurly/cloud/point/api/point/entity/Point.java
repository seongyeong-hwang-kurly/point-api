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
