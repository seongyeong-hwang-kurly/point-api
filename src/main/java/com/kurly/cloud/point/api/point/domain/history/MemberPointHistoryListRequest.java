package com.kurly.cloud.point.api.point.domain.history;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointHistoryListRequest {

  long memberNumber;
  boolean includeHidden;
  boolean includeMemo;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime regDateTimeFrom;
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime regDateTimeTo;

  int page;
  @Builder.Default
  int size = 10;
}
