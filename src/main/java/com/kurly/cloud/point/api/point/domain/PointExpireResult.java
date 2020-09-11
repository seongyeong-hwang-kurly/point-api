package com.kurly.cloud.point.api.point.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointExpireResult {
  long memberNumber;
  long totalExpired;
  List<Long> expiredPointSeq = new ArrayList<>();

  public void add(long pointSeq, long amount) {
    expiredPointSeq.add(pointSeq);
    totalExpired = totalExpired + amount;
  }
}
