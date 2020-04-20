/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.domain;

import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Builder
@Getter
@Setter
public class PublishPointRequest {
  @NotNull
  Long memberNumber;
  long orderNumber;
  @NotNull @Min(1)
  Integer point;
  float pointRatio;
  @NotNull
  Integer historyType;
  boolean payment;
  boolean settle;
  boolean unlimitedDate;
  LocalDateTime expireDate;

  @Builder.Default
  String memo = "";
  String detail;
  long actionMemberNumber;
  boolean hidden;

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
        .expireTime(getExpireDate())
        .build();
  }

  public boolean isUnlimitedDate() {
    return isSettle() || unlimitedDate;
  }

  public String getDetail() {
    if (Objects.isNull(detail)) {
      detail = MessageFormat.format("{0} 포인트 적립", isPayment() ? "유료" : "무료");
    }
    return detail;
  }

  public @Nullable LocalDateTime getExpireDate() {
    if (isUnlimitedDate()) {
      return null;
    }
    return Objects.isNull(expireDate) ?
        PointExpireDateCalculator.calculateDefault(LocalDateTime.now())
        :
        PointExpireDateCalculator.withEndOfDate(expireDate);
  }

  public @Nullable Integer getPoint() {
    if (Objects.nonNull(point)) {
      return point < 0 ? 0 : point;
    }
    return point;
  }
}
