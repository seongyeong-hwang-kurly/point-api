/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.domain.consume;

import com.kurly.cloud.point.api.point.entity.Point;
import java.util.Comparator;
import java.util.Objects;
import org.springframework.util.comparator.Comparators;

/**
 * 포인트를 먼저 소모해야 하는 순으로 정렬한다 <br/>
 * 1순위 유효기간 (유효기간이 없는 적립금(null)는 최후순위) <br/>
 * 2순위 결제하지 않은 유상적립금 <br/>
 * 3순위 결제한 유상적립금
 */
public class ConsumeOrderComparator implements Comparator<Point> {
  private static ConsumeOrderComparator instance;

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
