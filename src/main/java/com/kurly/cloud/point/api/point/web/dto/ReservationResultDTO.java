package com.kurly.cloud.point.api.point.web.dto;

import com.kurly.cloud.point.api.point.domain.publish.ReservationResultParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor(staticName = "create")
public class ReservationResultDTO {
  public static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
  private final long id;
  private final Long memberNumber;
  private final Long reservedPoint;
  private final Integer historyType;
  private final boolean payment;
  private final boolean settle;
  private final boolean applied;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private final ZonedDateTime startDateTime;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private final ZonedDateTime createDateTime;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private final ZonedDateTime expireDateTime;


  public static ReservationResultDTO from(ReservationResultParam param) {
    return ReservationResultDTO.create(
         param.getId(), param.getMemberNumber(),
         param.getReservedPoint(), param.getHistoryType(),
         param.isPayment(), param.isSettle(), param.isApplied(),
         ZonedDateTime.of(param.getStartedAt(), ZONE_ID),
         ZonedDateTime.of(param.getCreatedAt(), ZONE_ID),
         ZonedDateTime.of(param.getExpiredAt(), ZONE_ID)
    );
  }
}
