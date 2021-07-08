package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateRequest;
import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateResponse;
import com.kurly.cloud.point.api.point.service.CalculateReserveUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReserveCalculateController {
  private final CalculateReserveUseCase calculateReserveUseCase;

  @PostMapping(value = "/v1/reserve/calculate", consumes = MediaType.APPLICATION_JSON_VALUE)
  ReserveCalculateResponse calculate(@RequestBody ReserveCalculateRequest request) {
    return calculateReserveUseCase.calculate(request);
  }
}
