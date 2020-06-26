package com.kurly.cloud.point.api.point.domain.consume;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConsumePointRequest {
  @NotNull
  Long memberNumber;
  @NotNull
  Long orderNumber;
  @NotNull @Min(1)
  Integer point;
  boolean settle;

  public Integer getHistoryType() {
    return HistoryType.TYPE_100.getValue();
  }

  public String getDetail() {
    return HistoryType.TYPE_100.buildMessage(String.valueOf(orderNumber));
  }
}
