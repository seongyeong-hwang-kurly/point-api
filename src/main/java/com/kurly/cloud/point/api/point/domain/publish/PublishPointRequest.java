package com.kurly.cloud.point.api.point.domain.publish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishPointRequest {
  @NotNull
  Long memberNumber;
  @JsonIgnore
  long orderNumber;
  @NotNull @Min(1)
  Integer point;
  @JsonIgnore
  float pointRatio;
  @NotNull
  Integer historyType;
  boolean payment;
  boolean settle;
  @JsonIgnore
  boolean unlimitedDate;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  @Future
  ZonedDateTime expireDate;

  @Builder.Default
  String memo = "";
  String detail;
  @NotNull @Min(0)
  long actionMemberNumber;
  boolean hidden;

  public static class PublishPointRequestBuilder {
    public PublishPointRequestBuilder expireDate(LocalDateTime expireDate) {
      this.expireDate = DateTimeUtil.toZonedDateTime(expireDate);
      return this;
    }
  }

  /**
   * Entity로 변환.
   */
  public Point toEntity() {
    return Point.builder()
        .charge(getPoint())
        .remain(point)
        .memberNumber(memberNumber)
        .orderNumber(orderNumber)
        .pointRatio(pointRatio)
        .historyType(historyType)
        .refundType(0)
        .payment(payment)
        .settle(settle)
        .regTime(LocalDateTime.now())
        .expireTime(DateTimeUtil.toLocalDateTime(getExpireDate()))
        .build();
  }

  /**
   * 적립금.
   */
  public @Nullable Integer getPoint() {
    if (Objects.nonNull(point)) {
      return point < 0 ? 0 : point;
    }
    return point;
  }

  /**
   * 만료일.
   */
  public @Nullable ZonedDateTime getExpireDate() {
    if (isUnlimitedDate()) {
      return null;
    }
    return DateTimeUtil.toZonedDateTime(
        Objects.isNull(expireDate)
            ? PointExpireDateCalculator.calculateDefault(LocalDateTime.now())
            :
            PointExpireDateCalculator.withEndOfDate(expireDate.toLocalDateTime())
    );
  }

  public boolean isUnlimitedDate() {
    return isSettle() || unlimitedDate;
  }

  /**
   * 사유.
   */
  public String getDetail() {
    if (Objects.isNull(detail)) {
      detail = MessageFormat.format("{0} 적립금 적립", isPayment() ? "유료" : "무료");
    }
    return detail;
  }
}
