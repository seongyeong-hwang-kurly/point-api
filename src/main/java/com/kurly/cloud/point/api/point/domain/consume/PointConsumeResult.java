package com.kurly.cloud.point.api.point.domain.consume;

import java.util.ArrayList;
import java.util.List;

public class PointConsumeResult {
  int requested;
  List<ConsumedPoint> consumed = new ArrayList<>();

  public PointConsumeResult(int requested) {
    this.requested = requested;
  }

  public void add(long pointSeq, int amount, boolean settle) {
    consumed.add(new ConsumedPoint(pointSeq, amount, settle));
  }

  public void add(PointConsumeResult result) {
    add(result.getConsumed());
  }

  public void add(List<ConsumedPoint> anotherConsumed) {
    consumed.addAll(anotherConsumed);
  }

  public List<ConsumedPoint> getConsumed() {
    return consumed;
  }

  public int getTotalFreePointConsumed() {
    return consumed.stream().filter(consumedPoint -> !consumedPoint.isSettle())
        .mapToInt(ConsumedPoint::getConsumed).sum();
  }

  public int getTotalCashPointConsumed() {
    return consumed.stream().filter(ConsumedPoint::isSettle)
        .mapToInt(ConsumedPoint::getConsumed).sum();
  }

  public int getRemain() {
    return requested - getTotalConsumed();
  }

  public int getTotalConsumed() {
    return consumed.stream().mapToInt(ConsumedPoint::getConsumed).sum();
  }
}
