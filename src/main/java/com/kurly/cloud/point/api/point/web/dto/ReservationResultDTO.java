package com.kurly.cloud.point.api.point.web.dto;

import com.kurly.cloud.point.api.point.domain.publish.ReservationResultVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor(staticName = "create")
public class ReservationResultDTO {
  public static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
  private long id;
  private Long memberNumber;
  private long orderNumber;
  private Long reservedPoint;
  private float pointRatio;
  private Integer historyType;
  private boolean payment;
  private boolean settle;
  private boolean applied;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime startDateTime;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime createDateTime;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  private ZonedDateTime expireDateTime;


  public static ReservationResultDTO from(ReservationResultVO vo) {
    return ReservationResultDTO.create(
         vo.getId(), vo.getMemberNumber(), vo.getOrderNumber(),
         vo.getReservedPoint(), vo.getPointRatio(), vo.getHistoryType(),
         vo.isPayment(), vo.isSettle(), vo.isApplied(),
         ZonedDateTime.of(vo.getStartedAt(), ZONE_ID),
         ZonedDateTime.of(vo.getCreatedAt(), ZONE_ID),
         ZonedDateTime.of(vo.getExpiredAt(), ZONE_ID)
    );
  }
}
