package com.kurly.cloud.point.api.point.domain.publish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservePointRequestDTO extends PublishPointRequest {
  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  @FutureOrPresent
  ZonedDateTime startDate;

  public ReservationRequestVO toVO() {
    return ReservationRequestVO.create(
            memberNumber,
            orderNumber,
            point,
            pointRatio,
            historyType,
            payment,
            settle,
            unlimitedDate,
            expireDate,
            memo,
            detail,
            actionMemberNumber,
            hidden,
            startDate.toLocalDateTime()
    );
  }
}
