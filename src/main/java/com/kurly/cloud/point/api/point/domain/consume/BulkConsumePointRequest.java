package com.kurly.cloud.point.api.point.domain.consume;


import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkConsumePointRequest extends ConsumePointRequest {
  @NotNull
  Integer jobSeq;
}
