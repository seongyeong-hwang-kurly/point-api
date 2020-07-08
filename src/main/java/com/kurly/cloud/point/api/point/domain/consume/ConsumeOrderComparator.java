package com.kurly.cloud.point.api.point.domain.consume;

import com.kurly.cloud.point.api.point.entity.Point;
import java.util.Comparator;
import java.util.Objects;
import org.springframework.util.comparator.Comparators;

/**
 * 적립금을 먼저 소모해야 하는 순으로 정렬한다 <br/>
 * 1순위 유효기간 (유효기간이 없는 적립금(null)는 최후순위) <br/>
 * 2순위 결제하지 않은 유상적립금 <br/>
 * 3순위 결제한 유상적립금.
 */
public class ConsumeOrderComparator implements Comparator<Point> {
  private static ConsumeOrderComparator instance;

  /**
   * 인스턴스를 생성하거나 받아온다.
   */
  public static ConsumeOrderComparator getInstance() {
    if (Objects.isNull(instance)) {
      instance = new ConsumeOrderComparator();
    }
    return instance;
  }

  @Override
  public int compare(Point o1, Point o2) {

    int result = Comparators.nullsHigh().compare(o1.getExpireTime(), o2.getExpireTime());

    if (result == 0) {
      result = Boolean.compare(o1.isSettle(), o2.isSettle());
    }

    if (result == 0) {
      result = Boolean.compare(o1.isPayment(), o2.isPayment());
    }

    return result;
  }
}
