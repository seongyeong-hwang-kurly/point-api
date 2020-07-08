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