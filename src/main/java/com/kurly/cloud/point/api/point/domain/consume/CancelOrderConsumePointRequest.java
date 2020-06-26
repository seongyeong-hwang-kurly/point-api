/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.domain.consume;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderConsumePointRequest {
  @NotNull
  Long orderNumber;
  @NotNull
  Long memberNumber;
  @NotNull @Min(1)
  Integer point;
  long actionMemberNumber;

  public Integer getHistoryType() {
    return HistoryType.TYPE_2.getValue();
  }

  public String getDetail() {
    return HistoryType.TYPE_2.buildMessage(String.valueOf(orderNumber));
  }
}
