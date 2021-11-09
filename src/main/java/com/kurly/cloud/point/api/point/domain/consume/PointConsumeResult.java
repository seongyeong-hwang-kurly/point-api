package com.kurly.cloud.point.api.point.domain.consume;

import java.util.ArrayList;
import java.util.List;

public class PointConsumeResult {
  private final long requested;
  private final List<ConsumedPoint> consumed = new ArrayList<>();

  public PointConsumeResult(long requested) {
    this.requested = requested;
  }

  public void add(long pointSeq, long amount, boolean settle) {
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

  public long getTotalFreePointConsumed() {
    return consumed.stream().filter(consumedPoint -> !consumedPoint.isSettle())
        .mapToLong(ConsumedPoint::getConsumed).sum();
  }

  public long getTotalCashPointConsumed() {
    return consumed.stream().filter(ConsumedPoint::isSettle)
        .mapToLong(ConsumedPoint::getConsumed).sum();
  }

  public long getRemain() {
    return requested - getTotalConsumed();
  }

  public long getTotalConsumed() {
    return consumed.stream().mapToLong(ConsumedPoint::getConsumed).sum();
  }

  public boolean isNotComplete() {
    return requested != getTotalConsumed();
  }

}
