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
}
