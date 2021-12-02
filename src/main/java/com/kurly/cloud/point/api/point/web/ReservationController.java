package com.kurly.cloud.point.api.point.web;

import com.kurly.cloud.point.api.point.domain.publish.ReservationResultParam;
import com.kurly.cloud.point.api.point.domain.publish.ReservePointRequestDTO;
import com.kurly.cloud.point.api.point.service.impl.PointReservationDomainService;
import com.kurly.cloud.point.api.point.web.dto.ReservationResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final PointReservationDomainService pointReservationDomainService;

    @PostMapping(value = "/v2/reserve", consumes = MediaType.APPLICATION_JSON_VALUE)
    ReservationResultDTO reserve(
            @RequestBody @Valid ReservePointRequestDTO request
    ) {
        ReservationResultParam param = pointReservationDomainService.reserve(request.toParam());
        return ReservationResultDTO.from(param);
    }

    @GetMapping(value = "/v2/members/{memberNumber}/reserved-points", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ReservationResultDTO> getReservedPoints(@PathVariable("memberNumber") long memberNumber) {
        List<ReservationResultParam> reservedPointReturnParams = pointReservationDomainService.getReservedPoints(memberNumber);
        return convertToDtosFrom(reservedPointReturnParams);
    }

  private List<ReservationResultDTO> convertToDtosFrom(List<ReservationResultParam> reservedPointParams) {
    return reservedPointParams.stream().map(ReservationResultDTO::from).collect(Collectors.toList());
  }
}
