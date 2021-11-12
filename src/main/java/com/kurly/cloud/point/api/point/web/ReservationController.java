package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.domain.publish.ReservationResultVO;
import com.kurly.cloud.point.api.point.domain.publish.ReservePointRequestDTO;
import com.kurly.cloud.point.api.point.service.impl.PointReservationDomainService;
import com.kurly.cloud.point.api.point.web.dto.ReservationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
public class ReservationController {
  private final PointReservationDomainService pointReservationDomainService;

  @PostMapping(value = "/v2/reserve", consumes = MediaType.APPLICATION_JSON_VALUE)
  ReservationResultDTO reserve(
      @RequestBody @Valid ReservePointRequestDTO request
  ) {
    ReservationResultVO vo = pointReservationDomainService.reserve(request.toVO());
    return ReservationResultDTO.from(vo);
  }
}
