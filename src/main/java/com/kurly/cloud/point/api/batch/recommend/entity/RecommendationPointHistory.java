package com.kurly.cloud.point.api.batch.recommend.entity;

import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDataType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDelayType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointReason;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.batch.recommend.entity.converter.RecommendationDataTypeConverter;
import com.kurly.cloud.point.api.batch.recommend.entity.converter.RecommendationDelayTypeConverter;
import com.kurly.cloud.point.api.batch.recommend.entity.converter.RecommendationPointReasonConverter;
import com.kurly.cloud.point.api.batch.recommend.entity.converter.RecommendationPointStatusConverter;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mk_friend_recommendation_point_list_history")
public class RecommendationPointHistory {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  long id;

  @Builder.Default
  @Column(name = "point_type")
  int pointType = 1;

  @Column(name = "order_no")
  long orderNumber;

  @Column(name = "order_delivered_at")
  LocalDateTime orderDeliveredDateTime;

  @Column(name = "order_canceled_at")
  LocalDateTime orderCanceledDateTime;

  @Column(name = "order_address")
  String orderAddress;

  @Column(name = "order_member_no")
  long orderMemberNumber;

  @Column(name = "order_phone_number")
  String orderPhoneNumber;

  @Column(name = "recommendation_member_no")
  Long recommendationMemberNumber;

  @Column(name = "recommendation_phone_number")
  String recommendationPhoneNumber;

  @Column(name = "recommendation_address")
  String recommendationAddress;

  @Column(name = "recommendation_address_paid_count")
  int recommendationAddressPaidCount;

  @Column(name = "paid_point")
  long point;

  @Convert(converter = RecommendationPointReasonConverter.class)
  @Column(name = "paid_point_reason")
  RecommendationPointReason reason;

  @Convert(converter = RecommendationPointStatusConverter.class)
  @Column(name = "paid_point_status")
  RecommendationPointStatus status;

  @Convert(converter = RecommendationDataTypeConverter.class)
  @Column(name = "is_migration_data")
  RecommendationDataType type;

  @Column(name = "is_delayed")
  @Convert(converter = RecommendationDelayTypeConverter.class)
  RecommendationDelayType delayType;

  @Column(name = "created_at")
  LocalDateTime createDateTime;

  @Column(name = "updated_at")
  LocalDateTime updateDateTime;

  @Transient
  String orderMemberName;

  @Transient
  public void plusAddressPaidCount(int count) {
    this.recommendationAddressPaidCount = this.recommendationAddressPaidCount + count;
  }

  /**
   * 이름을 마스킹처리 한다.
   */
  @Transient
  public String getMaskedName(String name) {
    String maskedName = "*";
    if (name.length() > 1) {
      StringBuilder firstName = new StringBuilder(name.substring(0, 1));
      if (name.length() == 2) {
        return firstName + "*";
      }
      firstName.append("*".repeat(name.length() - 2));
      firstName.append(name.substring(name.length() - 1));
      maskedName = firstName.toString();
    }
    return maskedName;
  }

  @PrePersist
  void prePersist() {
    this.recommendationPhoneNumber = Objects.requireNonNullElse(this.recommendationPhoneNumber, "");
    this.recommendationAddress = Objects.requireNonNullElse(this.recommendationAddress, "");
    this.reason = Objects.requireNonNullElse(this.reason, RecommendationPointReason.DEFAULT);
  }
}
