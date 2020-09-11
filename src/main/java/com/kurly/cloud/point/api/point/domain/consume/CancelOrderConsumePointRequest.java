package com.kurly.cloud.point.api.point.domain.consume;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderConsumePointRequest {
  @NotNull
  Long orderNumber;
  @NotNull
  Long memberNumber;
  @NotNull @Min(1)
  Long point;
  long actionMemberNumber;

  @JsonIgnore
  public Integer getHistoryType() {
    return HistoryType.TYPE_2.getValue();
  }

  @JsonIgnore
  public String getDetail() {
    return HistoryType.TYPE_2.buildMessage(String.valueOf(orderNumber));
  }
}
