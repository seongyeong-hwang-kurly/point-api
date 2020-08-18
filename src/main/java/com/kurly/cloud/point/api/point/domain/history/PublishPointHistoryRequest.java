package com.kurly.cloud.point.api.point.domain.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublishPointHistoryRequest {
  List<Long> actionMemberNumber;
  List<Integer> historyType;
  @NotNull
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
  LocalDateTime regDateTimeFrom;
  @NotNull
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
  LocalDateTime regDateTimeTo;

  int page;
  @Builder.Default
  int size = 10;
}
