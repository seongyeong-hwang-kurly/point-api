package com.kurly.cloud.point.api.point.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PointExpireResult {
  long memberNumber;
  long totalExpired;
  LocalDateTime expiredAt;
  List<Long> expiredPointSeq = new ArrayList<>();

  public void add(long pointSeq, long amount) {
    expiredPointSeq.add(pointSeq);
    totalExpired = totalExpired + amount;
  }
}
