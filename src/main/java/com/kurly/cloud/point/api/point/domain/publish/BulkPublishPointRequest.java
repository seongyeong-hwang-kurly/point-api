package com.kurly.cloud.point.api.point.domain.publish;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkPublishPointRequest extends PublishPointRequest {
  @NotNull
  Integer jobSeq;
}
