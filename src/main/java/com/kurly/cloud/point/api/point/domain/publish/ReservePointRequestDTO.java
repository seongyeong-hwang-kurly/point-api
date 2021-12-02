package com.kurly.cloud.point.api.point.domain.publish;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReservePointRequestDTO extends PublishPointRequest {
  @NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  @FutureOrPresent
  ZonedDateTime startDate;

  public ReservePointRequestDTO(Long memberNumber,
                                long orderNumber,
                                @NotNull @Min(1) Long point,
                                float pointRatio,
                                @NotNull Integer historyType,
                                boolean payment,
                                boolean settle,
                                boolean unlimitedDate,
                                @Future ZonedDateTime expireDate,
                                String memo,
                                String detail,
                                @NotNull @Min(0) long actionMemberNumber,
                                boolean hidden,
                                ZonedDateTime startDate) {
    super(memberNumber,
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
            hidden);
    this.startDate = startDate;
  }

  public ReservationRequestParam toParam() {
    return ReservationRequestParam.create(
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
