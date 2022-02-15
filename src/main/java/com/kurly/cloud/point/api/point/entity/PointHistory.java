package com.kurly.cloud.point.api.point.entity;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.entity.converter.UnixTimestampConverter;
import com.kurly.cloud.point.api.point.exception.HistoryTypeNotFoundException;
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
@Table(name = "mk_point_list_history",
    indexes = {
        @Index(columnList = "ordno"),
        @Index(columnList = "action_m_no"),
        @Index(columnList = "reg_time")
    }
)
public class PointHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long seq;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "mk_point_list_seq")
  Point point;

  @Column(name = "ordno")
  long orderNumber;

  @NotNull
  @Column(name = "point")
  Long amount;

  @NotNull
  @Column(name = "history_type")
  Integer historyType;

  @Column(name = "detail")
  String detail;

  @Column(name = "memo")
  String memo;

  @Type(type = "numeric_boolean")
  @Column(name = "settle_flag")
  boolean settle;

  @Column(name = "action_m_no")
  long actionMemberNumber;

  @Convert(converter = UnixTimestampConverter.class)
  @Column(name = "reg_time")
  LocalDateTime regTime;

  @Column(name = "expired_at")
  LocalDateTime expiredAt;

  /**
   * 이력의 설명을 리턴 한다.
   */
  @Transient
  public String getHistoryTypeDesc() {
    try {
      return HistoryType.getByValue(this.historyType).getDesc();
    } catch (HistoryTypeNotFoundException e) {
      return "";
    }
  }
}
