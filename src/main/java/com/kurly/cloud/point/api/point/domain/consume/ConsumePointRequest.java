package com.kurly.cloud.point.api.point.domain.consume;

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
public class ConsumePointRequest {
  @NotNull
  Long memberNumber;
  long orderNumber;
  @NotNull @Min(1)
  Long point;
  @NotNull
  Integer historyType;
  boolean settle;

  @Builder.Default
  String memo = "";
  @NotNull
  String detail;
  @NotNull @Min(0)
  long actionMemberNumber;
}
