package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.exception.OrderPublishedNotFoundException;
import com.kurly.cloud.point.api.point.service.OrderPointUseCase;
import com.kurly.cloud.point.api.point.web.dto.PointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("PrivateOrderPointController")
public class OrderPointController {

  private final OrderPointUseCase orderPointUseCase;

  @GetMapping("/v1/order-published-amount/{orderNumber}")
  ResponseEntity<?> getOrderPublishedAmount(@PathVariable long orderNumber) {
    try {
      return ResponseEntity
          .ok(PointDto.fromEntity(orderPointUseCase.getOrderPublished(orderNumber)));
    } catch (OrderPublishedNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

}
