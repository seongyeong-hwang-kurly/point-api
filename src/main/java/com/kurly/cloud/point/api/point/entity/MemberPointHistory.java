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
@Table(name = "mk_point_info_history",
    indexes = {
        @Index(columnList = "mk_point_info_m_no"),
        @Index(columnList = "ordno"),
        @Index(columnList = "reg_time")
    }
)
public class MemberPointHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long seq;

  @NotNull
  @Column(name = "mk_point_info_m_no")
  Long memberNumber;

  @Column(name = "ordno")
  long orderNumber;

  @NotNull
  @Column(name = "history_type")
  Integer historyType;

  @Column(name = "total_point")
  long totalPoint;

  @Column(name = "free_point")
  long freePoint;

  @Column(name = "cash_point")
  long cashPoint;

  @Column(name = "detail")
  String detail;

  @Column(name = "memo")
  String memo;

  @Type(type = "numeric_boolean")
  @Column(name = "is_hidden")
  boolean hidden;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "reg_time")
  LocalDateTime regTime;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "expire_time")
  LocalDateTime expireTime;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "expired_at")
  LocalDateTime expiredAt;
}
