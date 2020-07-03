/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.adapter.out.controller.pri;

import com.kurly.cloud.api.common.domain.exception.ApiErrorResponse;
import com.kurly.cloud.point.api.point.adapter.out.dto.PointDto;
import com.kurly.cloud.point.api.point.exception.OrderPublishedNotFoundException;
import com.kurly.cloud.point.api.point.port.out.OrderPointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("PrivateOrderPointController")
public class OrderPointController {

  private final OrderPointPort orderPointPort;

  @GetMapping("/v1/order-published-amount/{orderNumber}")
  PointDto getOrderPublishedAmount(@PathVariable long orderNumber) {
    try {
      return PointDto.fromEntity(orderPointPort.getOrderPublished(orderNumber));
    } catch (OrderPublishedNotFoundException e) {
      throw new ApiErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

}
