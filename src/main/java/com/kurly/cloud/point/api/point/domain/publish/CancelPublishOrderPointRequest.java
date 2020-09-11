package com.kurly.cloud.point.api.point.domain.publish;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class CancelPublishOrderPointRequest {
  @NotNull
  Long memberNumber;
  @NotNull
  Long orderNumber;
  @NotNull @Min(1)
  Long point;
  @NotNull @Min(0)
  Long actionMemberNumber;

  @JsonIgnore
  public int getHistoryType() {
    return HistoryType.TYPE_101.getValue();
  }

  @JsonIgnore
  public String getDetail() {
    return HistoryType.TYPE_101.buildMessage(String.valueOf(orderNumber));
  }
}
