/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.domain;

import java.util.ArrayList;
import java.util.List;

public class PointConsumeResult {
  int requested;
  List<ConsumedPoint> consumed = new ArrayList<>();

  public PointConsumeResult(int requested) {
    this.requested = requested;
  }

  public void add(long pointSeq, int amount) {
    consumed.add(new ConsumedPoint(pointSeq, amount));
  }

  public int getTotalConsumed() {
    return consumed.stream().mapToInt(ConsumedPoint::getConsumed).sum();
  }

  public int getRemain() {
    return requested - getTotalConsumed();
  }
}