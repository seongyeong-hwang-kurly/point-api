package com.kurly.cloud.point.api.point.domain.consume;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrderConsumePointRequest {
  @NotNull
  Long memberNumber;
  @NotNull
  Long orderNumber;
  @NotNull
  Integer point;
  boolean settle;

  public Integer getHistoryType() {
    return HistoryType.TYPE_100.getValue();
  }

  public String getDetail() {
    return HistoryType.TYPE_100.buildMessage(orderNumber);
  }
}
