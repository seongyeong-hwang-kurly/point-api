package com.kurly.cloud.point.api.point.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointExpireResult {
  long memberNumber;
  int totalExpired;
  List<Long> expiredPointSeq = new ArrayList<>();

  public void add(long pointSeq, int amount) {
    expiredPointSeq.add(pointSeq);
    totalExpired = totalExpired + amount;
  }
}
